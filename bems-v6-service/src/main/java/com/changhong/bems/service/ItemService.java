package com.changhong.bems.service;

import com.changhong.bems.dao.ItemCorporationDao;
import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.ItemCorporation;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
            return OperateResult.operationSuccess("core_service_00028");
        } else {
            // 预算科目不存在!
            return OperateResult.operationFailure("item_00002");
        }
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
        }
        return ResultData.success();
    }

    /**
     * 分页查询公司预算科目
     *
     * @return 查询结果
     */
    public PageResult<Item> findPageByCorp(Search search, String corpCode) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
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
                itemCorp = itemMap.get(item.getId());
                if (Objects.nonNull(itemCorp)) {
                    item.setFrozen(itemCorp.getFrozen());
                }
            }
        }
        return pageResult;
    }

    /**
     * 根据code获取预算科目
     */
    public List<Item> getItems(String corpCode) {
        List<Item> itemList = dao.findAllUnfrozen();
        // 公司科目
        List<ItemCorporation> itemCorporations = itemCorporationDao.findListByProperty(ItemCorporation.FIELD_CORP_CODE, corpCode);
        if (CollectionUtils.isNotEmpty(itemCorporations)) {
            ItemCorporation itemCorp;
            Map<String, ItemCorporation> itemMap = itemCorporations.stream().collect(Collectors.toMap(ItemCorporation::getItemId, item -> item));
            for (Item item : itemList) {
                itemCorp = itemMap.get(item.getId());
                if (Objects.nonNull(itemCorp)) {
                    item.setFrozen(itemCorp.getFrozen());
                }
            }
        }
        return itemList;
    }
}