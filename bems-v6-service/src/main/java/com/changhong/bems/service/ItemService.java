package com.changhong.bems.service;

import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.OrderItem;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private SubjectService subjectService;

    @Override
    protected BaseEntityDao<Item> getDao() {
        return dao;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Item reference = dao.findFirstByProperty(Item.FIELD_REFERENCE_ID, id);
        if (Objects.nonNull(reference)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001");
        }
        DimensionAttribute attribute = dimensionAttributeService.findFirstByProperty(DimensionAttribute.FIELD_ITEM, id);
        if (Objects.nonNull(attribute)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001");
        }
        OrderItem orderItem = orderItemService.findFirstByProperty(OrderItem.FIELD_ITEM_ID, id);
        if (Objects.nonNull(orderItem)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001");
        }
        // TODO 导入明细
        return OperateResult.operationSuccess();
    }

    /**
     * 查询通用预算科目
     *
     * @return 查询结果
     */
    public List<Item> findByGeneral() {
        return dao.findListByProperty(Item.FIELD_TYPE, CategoryType.GENERAL);
    }

    /**
     * 根据预算主体查询私有预算科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<Item> findBySubject(String subjectId) {
        List<Item> itemList = new ArrayList<>();
        List<Item> generalList = findByGeneral();
        List<Item> privateList = dao.findListByProperty(Item.FIELD_SUBJECT_ID, subjectId);
        if (CollectionUtils.isEmpty(privateList)) {
            if (CollectionUtils.isNotEmpty(generalList)) {
                itemList.addAll(generalList);
            }
        } else {
            if (CollectionUtils.isNotEmpty(generalList)) {
                Set<String> ids = privateList.stream().map(Item::getReferenceId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(ids)) {
                    itemList.addAll(generalList.stream().filter(c -> !ids.contains(c.getId())).collect(Collectors.toList()));
                } else {
                    itemList.addAll(generalList);
                }
            }
            itemList.addAll(privateList);
        }
        return itemList;
    }

    /**
     * 引用通用预算类型
     *
     * @param subjectId 预算主体id
     * @param ids       通用预算类型id
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> reference(String subjectId, List<String> ids) {
        Subject subject = subjectService.findOne(subjectId);
        if (Objects.isNull(subject)) {
            // 预算主体不存在
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }

        List<Item> items = dao.findAllById(ids);
        for (Item item : items) {
            if (CategoryType.GENERAL == item.getType()) {
                Item privateItem = new Item();
                privateItem.setType(CategoryType.PRIVATE);
                privateItem.setSubjectId(subjectId);
                privateItem.setCode(item.getCode());
                privateItem.setName(item.getName());
                privateItem.setStrategyId(item.getStrategyId());
                privateItem.setStrategyName(item.getStrategyName());
                privateItem.setReferenceId(item.getId());
                this.save(privateItem);
            }
        }
        return ResultData.success();
    }

    /**
     * 冻结/解冻预算类型
     *
     * @param ids 预算类型id
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> frozen(List<String> ids, boolean frozen) {
        List<Item> items = dao.findAllById(ids);
        for (Item item : items) {
            item.setFrozen(frozen);
        }
        this.save(items);
        return ResultData.success();
    }
}