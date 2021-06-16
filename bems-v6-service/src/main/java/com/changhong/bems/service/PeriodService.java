package com.changhong.bems.service;

import com.changhong.bems.dao.PeriodDao;
import com.changhong.bems.dto.PeriodCode;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Period;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.limiter.support.lock.SeiLock;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.util.IdGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 预算期间(Period)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@Service
public class PeriodService extends BaseEntityService<Period> {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodService.class);
    @Autowired
    private PeriodDao dao;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;

    @Override
    protected BaseEntityDao<Period> getDao() {
        return dao;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Period period = dao.findOne(id);
        if (Objects.nonNull(period)) {
            if (PeriodType.CUSTOMIZE.equals(period.getType())) {
                if (!checkCustomizePeriod(id)) {
                    // 当前期间已被使用,禁止删除!
                    return OperateResult.operationFailure("period_00001");
                } else {
                    return OperateResult.operationSuccess();
                }
            } else {
                // 非自定义预算期间不允许删除
                return OperateResult.operationFailure("period_00003");
            }
        } else {
            // 预算期间不存在
            return OperateResult.operationFailure("period_00002");
        }
    }

    /**
     * 通过预算期间id查询所有可用的预算期间
     * 预算池溯源使用
     * 预算期间：
     * 1.自定义期间：以“=”匹配
     * 2.非自定义期间：按枚举@see {@link PeriodType}向下匹配（年度 < 半年度 < 季度 < 月度）
     * <p>
     * 优先使用自定义 > 月度 > 季度 > 半年度 > 年度
     *
     * @param periodId 预算期间id
     * @return 预算期间清单
     */
    public List<Period> findAvailablePeriods(String periodId) {
        List<Period> periods = new ArrayList<>();
        Period period = dao.findOne(periodId);
        if (Objects.nonNull(period)) {
            //自定义期间
            if (PeriodType.CUSTOMIZE == period.getType()) {
                periods.add(period);
            } else {
                Search search = Search.createSearch();
                //年份
                search.addFilter(new SearchFilter(Period.FIELD_YEAR, period.getYear()));
                //预算公司
                search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, period.getSubjectId()));
                //未关闭
                search.addFilter(new SearchFilter(Period.FIELD_CLOSED, Boolean.FALSE));
                Set<PeriodType> periodTypeSet = new HashSet<>();
                //年度
                if (PeriodType.ANNUAL == period.getType()) {
                    periodTypeSet.add(PeriodType.ANNUAL);
                } else if (PeriodType.SEMIANNUAL == period.getType()) {
                    //半年度
                    periodTypeSet.add(PeriodType.ANNUAL);
                    periodTypeSet.add(PeriodType.SEMIANNUAL);
                } else if (PeriodType.QUARTER == period.getType()) {
                    //季度
                    periodTypeSet.add(PeriodType.ANNUAL);
                    periodTypeSet.add(PeriodType.SEMIANNUAL);
                    periodTypeSet.add(PeriodType.QUARTER);
                } else if (PeriodType.MONTHLY == period.getType()) {
                    //月度
                    periodTypeSet.add(PeriodType.ANNUAL);
                    periodTypeSet.add(PeriodType.SEMIANNUAL);
                    periodTypeSet.add(PeriodType.QUARTER);
                    periodTypeSet.add(PeriodType.MONTHLY);
                } else {
                    return null;
                }
                search.addFilter(new SearchFilter(Period.FIELD_TYPE, periodTypeSet, SearchFilter.Operator.IN));
                //时间段(期间内)
                search.addFilter(new SearchFilter(Period.FIELD_START_DATE, period.getStartDate(), SearchFilter.Operator.LE));
                search.addFilter(new SearchFilter(Period.FIELD_END_DATE, period.getEndDate(), SearchFilter.Operator.GE));

                periods = findByFilters(search);
            }
        }
        return periods;
    }

    /**
     * 关闭过期预算期间调度定时任务
     * 定时任务执行，关闭过期预算期间
     *
     * @return 操作结果
     */
    @SeiLock(key = "'closing:overdue:period'")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> closingOverduePeriod() {
        int num = dao.closingOverduePeriod(LocalDate.now());
        LOG.info("关闭过期的预算期间: {}个", num);
        return ResultData.success();
    }

    /**
     * 按预算主体获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    public List<Period> findBySubject(String subjectId, PeriodType type) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        if (Objects.nonNull(type)) {
            search.addFilter(new SearchFilter(Period.FIELD_TYPE, type));
        }
        search.addSortOrder(SearchOrder.asc(Period.CREATED_DATE));
        return dao.findByFilters(search);
    }

    /**
     * 按预算主体获取期间(未关闭的)
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    public List<Period> findBySubjectUnclosed(String subjectId, PeriodType type) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Period.FIELD_TYPE, type));
        search.addFilter(new SearchFilter(Period.FIELD_CLOSED, Boolean.FALSE));
        search.addSortOrder(SearchOrder.asc(Period.CREATED_DATE));
        return dao.findByFilters(search);
    }

    /**
     * 按预算主体获取期间(未关闭的)
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    public List<Period> findBySubjectUnclosed(String subjectId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Period.FIELD_CLOSED, Boolean.FALSE));
        search.addSortOrder(SearchOrder.asc(Period.CREATED_DATE));
        return dao.findByFilters(search);
    }

    /**
     * 设置预算期间状态
     *
     * @param id     预算期间id
     * @param status 预算期间状态
     * @return 期间清单
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> setPeriodStatus(String id, boolean status) {
        dao.updateCloseStatus(id, status);
        return ResultData.success();
    }

    /**
     * 创建标准期间
     *
     * @return 期间清单
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> createNormalPeriod(String subjectId, int year, PeriodType[] periodTypes) {
        List<Period> periods = new ArrayList<>();
        for (PeriodType periodType : periodTypes) {
            periods.addAll(generateNormalPeriod(subjectId, year, periodType));
        }
        Set<String> existSet;
        List<Period> periodList = dao.findListByProperty(Period.FIELD_SUBJECT_ID, subjectId);
        if (CollectionUtils.isNotEmpty(periodList)) {
            existSet = periodList.stream().map(p -> p.getCode() + p.getYear()).collect(Collectors.toSet());
        } else {
            existSet = new HashSet<>();
        }
        for (Period period : periods) {
            if (!existSet.contains(period.getCode() + period.getYear())) {
                this.save(period);
            }
        }

        return ResultData.success();
    }

    /**
     * 创建/编辑自定义期间
     *
     * @param period 自定义预算期间
     * @return 期间清单
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> saveCustomizePeriod(Period period) {
        String id = period.getId();
        period.setType(PeriodType.CUSTOMIZE);
        if (StringUtils.isNotBlank(id)) {
            if (!checkCustomizePeriod(id)) {
                // 预算期间已被使用,禁止修改!
                return ResultData.fail(ContextUtil.getMessage("period_00004"));
            }
        } else {
            period.setCode("" + IdGenerator.nextId());
        }
        // TODO 检查同主体自定义期间交差

        this.save(period);
        return ResultData.success();
    }

    /**
     * 检查自定义期间是否配使用
     *
     * @return 检查结果
     */
    private boolean checkCustomizePeriod(String id) {
        DimensionAttribute attribute = dimensionAttributeService.findFirstByProperty(DimensionAttribute.FIELD_PERIOD, id);
        return !Objects.nonNull(attribute);
    }

    /**
     * 生成标准期间
     *
     * @param subjectId  预算主体id
     * @param year       年份
     * @param periodType 期间类型
     * @return 返回期间清单
     */
    private List<Period> generateNormalPeriod(String subjectId, int year, PeriodType periodType) {
        List<Period> periods = new ArrayList<>();
        Period period;
        LocalDate startDate;
        LocalDate endDate;
        switch (periodType) {
            case ANNUAL:
                period = new Period();
                period.setSubjectId(subjectId);
                period.setCode(PeriodCode.Y.name());
                period.setName(ContextUtil.getMessage("period_name_year", String.valueOf(year)));
                period.setType(periodType);
                period.setYear(year);
                startDate = LocalDate.of(year, 1, 1);
                period.setStartDate(startDate);
                endDate = startDate.withMonth(12);
                period.setEndDate(endDate.withDayOfMonth(endDate.lengthOfMonth()));
                periods.add(period);
                break;
            case SEMIANNUAL:
                period = new Period();
                period.setSubjectId(subjectId);
                period.setCode(PeriodCode.H1.name());
                period.setName(ContextUtil.getMessage("period_name_h1", String.valueOf(year)));
                period.setType(periodType);
                period.setYear(year);
                startDate = LocalDate.of(year, 1, 1);
                period.setStartDate(startDate);
                endDate = startDate.withMonth(6);
                period.setEndDate(endDate.withDayOfMonth(endDate.lengthOfMonth()));
                periods.add(period);

                period = new Period();
                period.setSubjectId(subjectId);
                period.setCode(PeriodCode.H2.name());
                period.setName(ContextUtil.getMessage("period_name_h2", String.valueOf(year)));
                period.setType(periodType);
                period.setYear(year);
                startDate = LocalDate.of(year, 7, 1);
                period.setStartDate(startDate);
                endDate = startDate.withMonth(12);
                period.setEndDate(endDate.withDayOfMonth(endDate.lengthOfMonth()));
                periods.add(period);
                break;
            case QUARTER:
                for (int q = 1; q < 5; q++) {
                    period = new Period();
                    period.setSubjectId(subjectId);
                    period.setCode(EnumUtils.getEnum(PeriodCode.class, "Q" + q).name());
                    period.setName(ContextUtil.getMessage("period_name_quarter", String.valueOf(year), q));
                    period.setType(periodType);
                    period.setYear(year);
                    startDate = LocalDate.of(year, q * 3 - 2, 1);
                    period.setStartDate(startDate);
                    endDate = startDate.withMonth(q * 3);
                    period.setEndDate(endDate.withDayOfMonth(endDate.lengthOfMonth()));
                    periods.add(period);
                }
                break;
            case MONTHLY:
                for (int m = 1; m < 13; m++) {
                    period = new Period();
                    period.setSubjectId(subjectId);
                    period.setCode(EnumUtils.getEnum(PeriodCode.class, "M" + m).name());
                    period.setName(ContextUtil.getMessage("period_name_monthly", String.valueOf(year), m));
                    period.setType(periodType);
                    period.setYear(year);
                    startDate = LocalDate.of(year, m, 1);
                    period.setStartDate(startDate);
                    endDate = startDate.withMonth(m);
                    period.setEndDate(endDate.withDayOfMonth(endDate.lengthOfMonth()));
                    periods.add(period);
                }
                break;
            default:
        }
        return periods;
    }
}