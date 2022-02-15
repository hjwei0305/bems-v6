package com.changhong.bems.service;

import com.changhong.bems.dao.PeriodDao;
import com.changhong.bems.dao.SubjectDao;
import com.changhong.bems.dto.PeriodCode;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Period;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
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
    private SubjectDao subjectDao;
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
                if (this.checkCustomizePeriod(id)) {
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
     * 关闭过期预算期间调度定时任务
     * 定时任务执行，关闭过期预算期间
     *
     * @return 操作结果
     */
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
    public List<Period> findBySubject(String subjectId, Integer year, PeriodType type) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        if (Objects.nonNull(year)) {
            search.addFilter(new SearchFilter(Period.FIELD_YEAR, year));
        }
        if (Objects.nonNull(type)) {
            search.addFilter(new SearchFilter(Period.FIELD_TYPE, type));
        }
        List<Period> periodList = dao.findByFilters(search);
        return periodList.stream()
                .sorted(Comparator.comparing(p -> p, this::sortedPeriod))
                .collect(Collectors.toList());
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
        List<Period> periodList = dao.findByFilters(search);
        return periodList.stream()
                .sorted(Comparator.comparing(p -> p, this::sortedPeriod))
                .collect(Collectors.toList());
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
        List<Period> periodList = dao.findByFilters(search);
        return periodList.stream()
                .sorted(Comparator.comparing(p -> p, this::sortedPeriod))
                .collect(Collectors.toList());
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
        Set<String> subjectIds = new HashSet<>();
        List<Period> periods = new ArrayList<>();
        if (StringUtils.isNotBlank(subjectId)) {
            for (PeriodType periodType : periodTypes) {
                periods.addAll(generateNormalPeriod(subjectId, year, periodType));
            }
            subjectIds.add(subjectId);
        } else {
            List<Subject> subjectList = subjectDao.findAllUnfrozen();
            if (CollectionUtils.isNotEmpty(subjectList)) {
                for (Subject subject : subjectList) {
                    subjectId = subject.getId();
                    for (PeriodType periodType : periodTypes) {
                        periods.addAll(generateNormalPeriod(subjectId, year, periodType));
                    }
                    subjectIds.add(subjectId);
                }
            }
        }

        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_YEAR, year));
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectIds, SearchFilter.Operator.IN));
        search.addFilter(new SearchFilter(Period.FIELD_TYPE, periodTypes, SearchFilter.Operator.IN));
        List<Period> periodList = dao.findByFilters(search);
        if (CollectionUtils.isNotEmpty(periodList)) {
            periods = periods.parallelStream().filter(period -> periodList.stream().noneMatch(old ->
                            period.getSubjectId().equals(old.getSubjectId())
                                    && period.getYear().equals(old.getYear())
                                    && period.getCode().equals(old.getCode())))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(periods)) {
            this.save(periods);
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
    public ResultData<Period> saveCustomizePeriod(Period period) {
        String id = period.getId();
        period.setType(PeriodType.CUSTOMIZE);

        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, period.getSubjectId()));
        search.addFilter(new SearchFilter(Period.FIELD_TYPE, PeriodType.CUSTOMIZE));
        search.addFilter(new SearchFilter(Period.FIELD_CLOSED, Boolean.FALSE));
        search.addFilter(new SearchFilter(Period.FIELD_START_DATE, period.getStartDate()));
        search.addFilter(new SearchFilter(Period.FIELD_END_DATE, period.getEndDate()));
        Period existed = dao.findFirstByFilters(search);
        if (Objects.nonNull(existed)) {
            // 已存在预算期间
            return ResultData.success(existed);
        }

        if (StringUtils.isNotBlank(id)) {
            if (this.checkCustomizePeriod(id)) {
                // 预算期间已被使用,禁止修改!
                return ResultData.fail(ContextUtil.getMessage("period_00004"));
            }
            existed = dao.findOne(id);
            if (Objects.isNull(existed)) {
                // 预算期间不存在!
                return ResultData.fail(ContextUtil.getMessage("period_00002"));
            }
            period.setId(id);
            period.setCode(existed.getCode());
        } else {
            period.setCode(String.valueOf(IdGenerator.nextId()));
        }

        this.save(period);
        return ResultData.success(period);
    }

    /**
     * 通过当前预算期间获取下一预算期间
     *
     * @param periodId 当前预算期间(非自定义类型预算期间)
     * @return 返回下一预算期间对象
     */
    public ResultData<Period> getNextPeriod(String periodId, boolean isAcrossYear) {
        Period currentPeriod = dao.findOne(periodId);
        if (Objects.isNull(currentPeriod)) {
            // 预算期间未找到
            return ResultData.fail(ContextUtil.getMessage("period_00002"));
        }
        //当前期间类型
        PeriodType currentPeriodType = currentPeriod.getType();
        if (PeriodType.CUSTOMIZE == currentPeriodType) {
            // 自定义期间类型无下级期间
            return ResultData.fail(ContextUtil.getMessage("period_00005"));
        }

        // 预算主体
        String subjectId = currentPeriod.getSubjectId();
        // 下一预算期间
        Period nextPeriod = null;
        // 通过标准期间的code规则递增
        String periodCode = currentPeriod.getCode();
        // 所属年
        Integer year = currentPeriod.getYear();
        //月度
        if (PeriodType.MONTHLY == currentPeriodType) {
            if (PeriodCode.M1.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M2.name(), year);
            } else if (PeriodCode.M2.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M3.name(), year);
            } else if (PeriodCode.M3.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M4.name(), year);
            } else if (PeriodCode.M4.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M5.name(), year);
            } else if (PeriodCode.M5.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M6.name(), year);
            } else if (PeriodCode.M6.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M7.name(), year);
            } else if (PeriodCode.M7.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M8.name(), year);
            } else if (PeriodCode.M8.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M9.name(), year);
            } else if (PeriodCode.M9.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M10.name(), year);
            } else if (PeriodCode.M10.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M11.name(), year);
            } else if (PeriodCode.M11.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M12.name(), year);
            } else if (PeriodCode.M12.name().equals(periodCode)) {
                // 跨年度结转控制
                if (isAcrossYear) {
                    nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.M1.name(), year + 1);
                } else {
                    // 不允许跨年度获取下一期间
                    return ResultData.fail(ContextUtil.getMessage("pool_00016"));
                }
            }
        }
        //季度
        else if (PeriodType.QUARTER == currentPeriodType) {
            if (PeriodCode.Q1.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.Q2.name(), year);
            } else if (PeriodCode.Q2.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.Q3.name(), year);
            } else if (PeriodCode.Q3.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.Q4.name(), year);
            } else if (PeriodCode.Q4.name().equals(periodCode)) {
                // 跨年度结转控制
                if (isAcrossYear) {
                    nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.Q1.name(), year + 1);
                } else {
                    // 不允许跨年度获取下一期间
                    return ResultData.fail(ContextUtil.getMessage("pool_00016"));
                }
            }
        }
        //半年度
        else if (PeriodType.SEMIANNUAL == currentPeriodType) {
            if (PeriodCode.H1.name().equals(periodCode)) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.H2.name(), year);
            } else if (PeriodCode.H2.name().equals(periodCode)) {
                // 跨年度结转控制
                if (isAcrossYear) {
                    nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.H1.name(), year + 1);
                } else {
                    // 不允许跨年度获取下一期间
                    return ResultData.fail(ContextUtil.getMessage("pool_00016"));
                }
            }
        }
        //年度
        else if (PeriodType.ANNUAL == currentPeriodType) {
            // 跨年度结转控制
            if (isAcrossYear) {
                nextPeriod = this.findByCodeAndYear(subjectId, PeriodCode.Y.name(), year + 1);
            } else {
                // 不允许跨年度获取下一期间
                return ResultData.fail(ContextUtil.getMessage("pool_00016"));
            }
        }
        if (Objects.isNull(nextPeriod)) {
            // 未找到[{}]的下一期间
            return ResultData.fail(ContextUtil.getMessage("period_00006", currentPeriod.getName()));
        } else {
            return ResultData.success(nextPeriod);
        }
    }

    /**
     * 按预算主体获取期间(未关闭的)
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    private Period findByCodeAndYear(String subjectId, String code, Integer year) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Period.FIELD_CODE, code));
        //年份
        search.addFilter(new SearchFilter(Period.FIELD_YEAR, year));
        search.addFilter(new SearchFilter(Period.FIELD_CLOSED, Boolean.FALSE));
        return dao.findFirstByFilters(search);
    }

    /**
     * 检查自定义期间是否被使用
     *
     * @return 检查结果
     */
    private boolean checkCustomizePeriod(String id) {
        DimensionAttribute attribute = dimensionAttributeService.getFirstByProperty(DimensionAttribute.FIELD_PERIOD, id);
        return Objects.nonNull(attribute);
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
                    if (m < 10) {
                        period.setName(ContextUtil.getMessage("period_name_monthly", String.valueOf(year), "0" + m));
                    } else {
                        period.setName(ContextUtil.getMessage("period_name_monthly", String.valueOf(year), m));
                    }
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

    /**
     * 预算期间排序规则
     *
     * @param p1 期间1
     * @param p2 期间2
     * @return 比较器值，如果较小则为负，如果较大则为正
     */
    private int sortedPeriod(Period p1, Period p2) {
        if (p1.getType() == p2.getType()) {
            return p1.getStartDate().compareTo(p2.getStartDate());
        } else {
            return p1.getType().compareTo(p2.getType());
        }
    }
}