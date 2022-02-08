package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectPeriodDao;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.entity.SubjectPeriod;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * 冻结/解冻预算类型
     *
     * @param ids 预算主体科目id
     * @return 操作结果
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> frozen(List<String> ids, boolean frozen) {
        List<SubjectPeriod> items = dao.findAllById(ids);
        for (SubjectPeriod item : items) {
            item.setFrozen(frozen);
        }
        this.save(items);
        return ResultData.success();
    }

    /**
     * 根据预算主体查询私有预算主体科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<SubjectPeriod> findBySubject(String subjectId) {
        return dao.findListByProperty(SubjectItem.FIELD_SUBJECT_ID, subjectId);
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