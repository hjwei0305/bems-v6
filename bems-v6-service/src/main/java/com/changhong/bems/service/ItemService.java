package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.ItemCorporationDao;
import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.ItemCorporation;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预算科目(Item)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
public class ItemService extends BaseEntityService<Item> {
    @Autowired
    private ItemDao dao;
    @Autowired
    private ItemCorporationDao itemCorporationDao;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected BaseEntityDao<Item> getDao() {
        return dao;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String id) {
        Item item = dao.findOne(id);
        if (Objects.nonNull(item)) {
            DimensionAttribute attribute = dimensionAttributeService.getFirstByProperty(DimensionAttribute.FIELD_ITEM, item.getCode());
            if (Objects.nonNull(attribute)) {
                // 当前科目已被使用,禁止删除!
                return OperateResult.operationFailure("item_00001");
            }
            // 删除公司下维护的数据
            itemCorporationDao.deleteByItemId(id);
            // 删除科目数据
            dao.delete(id);
            // 清空缓存
            this.cleanItemCache();
            return OperateResult.operationSuccess("core_service_00028");
        } else {
            // 预算科目不存在!
            return OperateResult.operationFailure("item_00002");
        }
    }

    /**
     * 数据保存操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Item> save(Item entity) {
        OperateResultWithData<Item> result = super.save(entity);
        if (result.successful()) {
            // 清空缓存
            this.cleanItemCache();
        }
        return result;
    }

    /**
     * 导入预算科目
     *
     * @param items 预算科目清单
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> importItem(List<Item> items) {
        this.save(items);
        // 清空缓存
        this.cleanItemCache();
        return ResultData.success();
    }

    /**
     * 禁用预算科目
     *
     * @param ids      预算科目id
     * @param disabled 为true时禁用,反之启用
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> disabled(String corpCode, Set<String> ids, boolean disabled) {
        if (StringUtils.isNotBlank(corpCode) && !StringUtils.equalsIgnoreCase(CategoryType.GENERAL.name(), corpCode)) {
            final Map<String, ItemCorporation> itemMap;
            // 公司科目禁用启用操作
            List<ItemCorporation> itemList = itemCorporationDao.findListByProperty(ItemCorporation.FIELD_CORP_CODE, corpCode);
            if (CollectionUtils.isEmpty(itemList)) {
                itemMap = new HashMap<>();
            } else {
                itemMap = itemList.stream().collect(Collectors.toMap(ItemCorporation::getItemId, obj -> obj));
            }
            List<ItemCorporation> itemCorporations = ids.stream().map(id -> {
                ItemCorporation itemCorporation = itemMap.get(id);
                if (Objects.isNull(itemCorporation)) {
                    itemCorporation = new ItemCorporation();
                    itemCorporation.setItemId(id);
                    itemCorporation.setCorpCode(corpCode);
                }
                itemCorporation.setFrozen(disabled);
                return itemCorporation;
            }).collect(Collectors.toList());
            itemCorporationDao.save(itemCorporations);
        } else {
            // 通用科目禁用启用操作
            dao.disabledGeneral(ids, disabled);
            // 移除公司配置科目
            itemCorporationDao.deleteByItemIdIn(ids);
        }

        // 清空缓存
        this.cleanItemCache();
        return ResultData.success();
    }

    /**
     * 分页查询公司预算科目
     * 预算科目公司私有功能分页查询
     *
     * @return 查询结果
     */
    public PageResult<Item> findPageByCorp(Search search, String corpCode) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 分页获取通用科目清单
        PageResult<Item> pageResult = this.findByPage(search);
        if (pageResult.getRecords() > 0) {
            Map<String, ItemCorporation> itemMap;
            List<Item> itemList = pageResult.getRows();
            // 公司科目
            List<ItemCorporation> itemCorporations = itemCorporationDao.findListByProperty(ItemCorporation.FIELD_CORP_CODE, corpCode);
            if (CollectionUtils.isNotEmpty(itemCorporations)) {
                itemMap = itemCorporations.stream().collect(Collectors.toMap(ItemCorporation::getItemId, item -> item));
            } else {
                itemMap = new HashMap<>();
            }
            ItemCorporation itemCorp;
            for (Item item : itemList) {
                // 通用科目被禁用,同步标示禁用公司科目
                if (Boolean.TRUE.equals(item.getFrozen())) {
                    continue;
                }
                itemCorp = itemMap.get(item.getId());
                if (Objects.nonNull(itemCorp)) {
                    // 设置公司科目禁用状态
                    item.setFrozen(itemCorp.getFrozen());
                }
            }
        }
        return pageResult;
    }


    /**
     * 分页查询公司可用的预算科目
     * 预算策略执行策略分页查询
     *
     * @return 查询结果
     */
    public PageResult<Item> findPageUsableByCorp(Search search, String corpCode) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 公司禁用的科目id清单
        Set<String> itemIds = null;

        Search corpSearch = Search.createSearch();
        corpSearch.addFilter(new SearchFilter(ItemCorporation.FIELD_CORP_CODE, corpCode));
        // 公司禁用的科目
        corpSearch.addFilter(new SearchFilter(ItemCorporation.FROZEN, Boolean.TRUE));
        List<ItemCorporation> itemCorporations = itemCorporationDao.findByFilters(corpSearch);
        if (CollectionUtils.isNotEmpty(itemCorporations)) {
            itemIds = itemCorporations.stream().map(ItemCorporation::getItemId).collect(Collectors.toSet());
        }
        if (CollectionUtils.isNotEmpty(itemIds)) {
            search.addFilter(new SearchFilter(Item.ID, itemIds, SearchFilter.Operator.NOTIN));
        }
        // 可用的预算科目
        search.addFilter(new SearchFilter(Item.FROZEN, Boolean.FALSE));

        // 分页获取通用科目清单
        return this.findByPage(search);
    }

    /**
     * 根据code获取预算科目
     */
    public Item findByCode(String itemCode) {
        return dao.findFirstByProperty(Item.CODE_FIELD, itemCode);
    }

    /**
     * 根据预算主体查询私有预算主体科目(不包含冻结状态的)
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @SuppressWarnings("unchecked")
    public List<Item> findItemsBySubject(String subjectId) {
        // 优先读取缓存
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.ITEM_CACHE_KEY_PREFIX + subjectId);
        List<Item> itemList = (List<Item>) operations.get();
        if (CollectionUtils.isEmpty(itemList)) {
            Subject subject = subjectService.getSubject(subjectId);
            if (Objects.nonNull(subject)) {
                // 按公司代码获取科目

                // 获取所有可用科目
                itemList = dao.findAllUnfrozen();
                // 公司科目
                List<ItemCorporation> itemCorporations = itemCorporationDao.findListByProperty(ItemCorporation.FIELD_CORP_CODE, subject.getCorporationCode());
                if (CollectionUtils.isNotEmpty(itemCorporations)) {
                    ItemCorporation itemCorp;
                    Map<String, ItemCorporation> itemMap = itemCorporations.stream().collect(Collectors.toMap(ItemCorporation::getItemId, item -> item));
                    for (Item item : itemList) {
                        // 通用科目被禁用,同步标示禁用公司科目
                        if (Boolean.TRUE.equals(item.getFrozen())) {
                            continue;
                        }
                        itemCorp = itemMap.get(item.getId());
                        if (Objects.nonNull(itemCorp)) {
                            item.setFrozen(itemCorp.getFrozen());
                        }
                    }
                }
                itemList = itemList.stream().filter(item -> Boolean.FALSE.equals(item.getFrozen())).collect(Collectors.toList());

                // 写入缓存
                operations.set(itemList, 3, TimeUnit.DAYS);
            } else {
                itemList = new ArrayList<>();
            }
        }
        return itemList;
    }

    /**
     * 清除策略缓存
     */
    private void cleanItemCache() {
        CompletableFuture.runAsync(() -> {
            try {
                Set<String> keys = redisTemplate.keys(Constants.ITEM_CACHE_KEY_PREFIX.concat(":*"));
                if (CollectionUtils.isNotEmpty(keys)) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                LogUtil.error("清空预算科目缓存异常.", e);
            }
        });
    }
}