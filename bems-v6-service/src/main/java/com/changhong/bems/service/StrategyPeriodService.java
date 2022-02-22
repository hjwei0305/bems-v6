package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.PeriodDao;
import com.changhong.bems.dao.StrategyPeriodDao;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.entity.StrategyPeriod;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预算期间类型策略(StrategyPeriod)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
@CacheConfig(cacheNames = Constants.STRATEGY_PERIOD_CACHE_KEY_PREFIX)
public class StrategyPeriodService {
    @Autowired
    private StrategyPeriodDao dao;
    @Autowired
    private PeriodDao periodDao;

    /**
     * 数据保存操作
     */
    @CacheEvict(key = "#entity.subjectId")
    @Transactional(rollbackFor = Exception.class)
    public void save(StrategyPeriod entity) {
        dao.save(entity);
    }


    /**
     * 创建预算主体时,初始化预算主体科目
     *
     * @param subjectId 预算主体id
     */
    @Transactional(rollbackFor = Exception.class)
    public void initStrategyPeriod(String subjectId) {
        String tenantCode = ContextUtil.getTenantCode();
        PeriodType[] periodTypes = PeriodType.values();
        List<StrategyPeriod> list = new ArrayList<>(periodTypes.length);
        StrategyPeriod subjectPeriod;
        for (PeriodType periodType : periodTypes) {
            subjectPeriod = new StrategyPeriod();
            subjectPeriod.setTenantCode(tenantCode);
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
        dao.save(list);
    }

    /**
     * id查询预算主体科目
     *
     * @param id id
     * @return 查询结果
     */
    public StrategyPeriod findOne(String id) {
        return dao.findOne(id);
    }

    /**
     * 根据预算主体查询私有预算主体科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<StrategyPeriod> findBySubject(String subjectId) {
        List<StrategyPeriod> list = dao.findListByProperty(StrategyItem.FIELD_SUBJECT_ID, subjectId);
        if (CollectionUtils.isNotEmpty(list)) {
            Set<Object> enabledPeriodTypes = periodDao.getEnabledPeriodType(subjectId, LocalDate.now().getYear());
            System.out.println(enabledPeriodTypes);
            return list.stream().filter(sp -> enabledPeriodTypes.contains(sp.getPeriodType())).sorted(Comparator.comparingInt(o -> o.getPeriodType().ordinal())).collect(Collectors.toList());
        }
        return list;
    }

    /**
     * 按预算主体id和科目代码获取科目
     *
     * @param subjectId  预算主体id
     * @param periodType 科目代码
     * @return 返回科目
     */
    @Cacheable(key = "#subjectId + ':' + #periodType.name")
    public StrategyPeriod getSubjectPeriod(String subjectId, PeriodType periodType) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(StrategyPeriod.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(StrategyPeriod.FIELD_PERIOD_TYPE, periodType));
        return dao.findFirstByFilters(search);
    }
}