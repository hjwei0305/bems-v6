package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.StrategyItemDao;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.log.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置预算科目为主体私有
     *
     * @return 设置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> turnPrivate(String subjectId, String itemCode, boolean isPrivate) {
        StrategyItem strategyItem = this.getSubjectItem(subjectId, itemCode);
        if (isPrivate) {
            // 如果存在直接返回
            if (Objects.isNull(strategyItem)) {
                Subject subject = subjectService.getSubject(subjectId);
                if (Objects.isNull(subject)) {
                    // 主体[{0}]不存在!
                    return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
                }
                Item item = itemService.findByCode(itemCode);
                if (Objects.isNull(item)) {
                    // 科目[{0}]不存在!
                    return ResultData.fail(ContextUtil.getMessage("item_00003", itemCode));
                }
                strategyItem = new StrategyItem();
                strategyItem.setTenantCode(ContextUtil.getTenantCode());
                strategyItem.setSubjectId(subjectId);
                strategyItem.setCode(itemCode);
                strategyItem.setName(item.getName());
                // 默认使用当前主体的策略进行初始化
                strategyItem.setStrategyId(subject.getStrategyId());
                strategyItem.setStrategyName(subject.getStrategyName());
                dao.save(strategyItem);
            }
        } else {
            // 如果存在则删除
            if (Objects.nonNull(strategyItem)) {
                dao.delete(strategyItem);
            }
        }
        return ResultData.success();
    }

    /**
     * 配置预算科目执行策略
     *
     * @return 配置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> setStrategy(String subjectId, String itemCode, String strategyId) {
        StrategyItem strategyItem = this.getSubjectItem(subjectId, itemCode);
        if (Objects.isNull(strategyItem)) {
            // 请先转为主体私有再设置执行策略
            return ResultData.fail(ContextUtil.getMessage("item_00004"));
        }
        strategyItem.setStrategyId(strategyId);
        strategyItem.setStrategyName(strategyService.getNameByCode(strategyId));
        dao.save(strategyItem);
        // 清除策略缓存
        subjectService.cleanStrategyCache(subjectId, itemCode);
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
            // 公司可用的预算科目
            PageResult<Item> itemPageResult = itemService.findPageUsableByCorp(subject.getCorporationCode(), search);
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
                        strategyItem.setId(si.getId());
                        strategyItem.setStrategyId(si.getStrategyId());
                        strategyItem.setStrategyName(si.getStrategyName());
                    }
                    return strategyItem;
                }).collect(Collectors.toList());

                pageResult.setRows(strategyItemList);
            }
        } else {
            LogUtil.error("预算主体[{}]不存在.", subjectId);
            pageResult = new PageResult<>();
        }
        return pageResult;
    }

    /**
     * 获取预算执行控制策略
     *
     * @param subjectId 预算主体id
     * @param itemCode  预算科目代码
     * @return 预算执行控制策略
     */
    public ResultData<StrategyDto> getStrategy(String subjectId, String itemCode) {
        BoundValueOperations<String, Object> operations =
                redisTemplate.boundValueOps(Constants.STRATEGY_CACHE_KEY_PREFIX + subjectId + ":" + itemCode);
        // 预算主体策略
        StrategyDto strategy = (StrategyDto) operations.get();
        if (Objects.isNull(strategy)) {
            // 预算主体科目
            StrategyItem subjectItem = this.getSubjectItem(subjectId, itemCode);
            if (Objects.nonNull(subjectItem)) {
                if (StringUtils.isNotBlank(subjectItem.getStrategyId())) {
                    // 预算主体科目策略
                    strategy = strategyService.getByCode(subjectItem.getStrategyId());
                }
            }
            if (Objects.isNull(strategy)) {
                Subject subject = subjectService.getSubject(subjectId);
                if (Objects.nonNull(subject)) {
                    strategy = strategyService.getByCode(subject.getStrategyId());
                    if (Objects.isNull(strategy)) {
                        // 预算占用时,未找到预算主体[{0}]的预算科目[{1}]
                        return ResultData.fail(ContextUtil.getMessage("pool_00010", subjectId, itemCode));
                    }
                } else {
                    // 预算主体[{0}]不存在!
                    return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
                }
            }
            // 写入缓存
            operations.set(strategy, 3, TimeUnit.DAYS);
        }
        return ResultData.success(strategy);
    }

    /**
     * 按预算主体id和科目代码获取科目
     *
     * @param subjectId 预算主体id
     * @param itemCode  科目代码
     * @return 返回科目
     */
    private StrategyItem getSubjectItem(String subjectId, String itemCode) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(StrategyItem.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(StrategyItem.FIELD_CODE, itemCode));
        return dao.findFirstByFilters(search);
    }
}