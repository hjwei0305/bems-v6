package com.changhong.bems.service;

import com.changhong.bems.dao.StrategyItemDao;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 执行策略(StrategyItem)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
public class StrategyItemService {
    @Autowired
    private StrategyItemDao dao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ItemService itemService;

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String id) {
        StrategyItem entity = dao.findOne(id);
        if (Objects.nonNull(entity)) {
            // 清除策略缓存
            subjectService.cleanStrategyCache(entity.getSubjectId(), entity.getCode());
            dao.delete(entity);
            return OperateResult.operationSuccess("core_service_00028");
        } else {
            return OperateResult.operationWarning("core_service_00029");
        }
    }

    /**
     * 数据保存操作
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ResultData<StrategyItem> save(StrategyItem entity) {
        // 清除策略缓存
        subjectService.cleanStrategyCache(entity.getSubjectId(), entity.getCode());
        entity.setTenantCode(ContextUtil.getTenantCode());
        dao.save(entity);
        return ResultData.success();
    }

    /**
     * 按主体获取预算科目执行策略(预算策略菜单功能使用)
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public PageResult<StrategyItem> findPageByCorp(String subjectId, Search search) {
        PageResult<StrategyItem> pageResult;
        Subject subject = subjectService.getSubject(subjectId);
        if (Objects.nonNull(subject)) {
            PageResult<Item> itemPageResult = itemService.findPageByCorp(search, subject.getCorporationCode());
            pageResult = new PageResult<>(itemPageResult);
            if (itemPageResult.getRecords() > 0) {
                Map<String, StrategyItem> strategyItemMap;
                List<StrategyItem> strategyItems = dao.findListByProperty(StrategyItem.FIELD_SUBJECT_ID, subjectId);
                if (CollectionUtils.isNotEmpty(strategyItems)) {
                    strategyItemMap = strategyItems.stream().collect(Collectors.toMap(StrategyItem::getCode, item -> item));
                } else {
                    strategyItemMap = new HashMap<>();
                }
                List<Item> itemList = itemPageResult.getRows();
                List<StrategyItem> strategyItemList = itemList.stream().map(item -> {
                    StrategyItem strategyItem = new StrategyItem();
                    strategyItem.setSubjectId(subjectId);
                    strategyItem.setCode(item.getCode());
                    strategyItem.setName(item.getName());
                    StrategyItem si = strategyItemMap.get(item.getCode());
                    if (Objects.nonNull(si)) {
                        strategyItem.setStrategyId(si.getStrategyId());
                        strategyItem.setStrategyName(si.getStrategyName());
                    }
                    return strategyItem;
                }).collect(Collectors.toList());

                pageResult.setRows(strategyItemList);
            }
        } else {
            pageResult = new PageResult<>();
        }
        return pageResult;
    }

    /**
     * 根据预算主体查询私有预算主体科目(不包含冻结状态的)
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Cacheable(key = "#subjectId")
    public List<StrategyItem> findBySubjectUnfrozen(String subjectId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(StrategyItem.FIELD_SUBJECT_ID, subjectId));
        search.addSortOrder(SearchOrder.asc(StrategyItem.FIELD_CODE));
        // return findByFilters(search);
        return null;
    }

    /**
     * 按预算主体id和科目代码获取科目
     *
     * @param subjectId 预算主体id
     * @param itemCode  科目代码
     * @return 返回科目
     */
    @Cacheable(key = "#subjectId + ':' + #itemCode")
    public StrategyItem getSubjectItem(String subjectId, String itemCode) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(StrategyItem.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(StrategyItem.FIELD_CODE, itemCode));
        return dao.findFirstByFilters(search);
    }
}