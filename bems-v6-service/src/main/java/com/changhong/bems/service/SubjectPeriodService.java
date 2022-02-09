package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectPeriodDao;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.entity.SubjectPeriod;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算期间类型策略(SubjectPeriod)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
@CacheConfig(cacheNames = SubjectPeriodService.CACHE_KEY)
public class SubjectPeriodService extends BaseEntityService<SubjectPeriod> {
    @Autowired
    private SubjectPeriodDao dao;

    public static final String CACHE_KEY = "bems-v6:subjectPeriod";

    @Override
    protected BaseEntityDao<SubjectPeriod> getDao() {
        return dao;
    }

    /**
     * 数据保存操作
     */
    @Override
    @CacheEvict(key = "#entity.subjectId")
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<SubjectPeriod> save(SubjectPeriod entity) {
        return super.save(entity);
    }

    /**
     * 主键删除
     *
     * @param s 主键
     * @return 返回操作结果对象
     */
    @Override
    public OperateResult delete(String s) {
        return OperateResult.operationWarning("not support");
    }

    /**
     * 根据预算主体查询私有预算主体科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Transactional(rollbackFor = Exception.class)
    public List<SubjectPeriod> findBySubject(String subjectId) {
        List<SubjectPeriod> list = dao.findListByProperty(SubjectItem.FIELD_SUBJECT_ID, subjectId);
        if (CollectionUtils.isEmpty(list)) {
            PeriodType[] periodTypes = PeriodType.values();
            list = new ArrayList<>(periodTypes.length);
            SubjectPeriod subjectPeriod;
            for (PeriodType periodType : periodTypes) {
                subjectPeriod = new SubjectPeriod();
                subjectPeriod.setSubjectId(subjectId);
                subjectPeriod.setPeriodType(periodType);
                boolean use;
                boolean roll;
                switch (periodType) {
                    case ANNUAL:
                        // 默认年度期间,不允许结转,不允许业务直接使用
                        use = Boolean.FALSE;
                        roll = Boolean.FALSE;
                        break;
                    case CUSTOMIZE:
                        // 默认自定义期间,不允许结转,允许业务直接使用
                        use = Boolean.TRUE;
                        roll = Boolean.FALSE;
                        break;
                    default:
                        // 允许结转,允许业务直接使用
                        use = Boolean.TRUE;
                        roll = Boolean.TRUE;
                }
                subjectPeriod.setRoll(roll);
                subjectPeriod.setUse(use);
                list.add(subjectPeriod);
            }
            try {
                this.save(list);
            } catch (Exception e) {
                // 回滚事务
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return list.stream().sorted(Comparator.comparingInt(o -> o.getPeriodType().ordinal())).collect(Collectors.toList());
    }

    /**
     * 根据预算主体查询私有预算主体科目(不包含冻结状态的)
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Cacheable(key = "#subjectId")
    public List<SubjectPeriod> findBySubjectUnfrozen(String subjectId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(SubjectPeriod.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(SubjectPeriod.FROZEN, Boolean.FALSE));
        search.addSortOrder(SearchOrder.asc(SubjectPeriod.FIELD_PERIOD_TYPE));
        return findByFilters(search);
    }

    /**
     * 按预算主体id和科目代码获取科目
     *
     * @param subjectId  预算主体id
     * @param periodType 科目代码
     * @return 返回科目
     */
    @Cacheable(key = "#subjectId + ':' + #periodType.name")
    public SubjectPeriod getSubjectPeriod(String subjectId, PeriodType periodType) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(SubjectPeriod.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(SubjectPeriod.FIELD_PERIOD_TYPE, periodType));
        return dao.findFirstByFilters(search);
    }
}