package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectItemDao;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预算主体科目(SubjectItem)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
public class SubjectItemService extends BaseEntityService<SubjectItem> {
    @Autowired
    private SubjectItemDao dao;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private OrderDetailService orderItemService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ItemService itemService;

    @Override
    protected BaseEntityDao<SubjectItem> getDao() {
        return dao;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @Override
    public OperateResult delete(String id) {
        SubjectItem entity = findOne(id);
        if (Objects.nonNull(entity)) {
            DimensionAttribute attribute = dimensionAttributeService.findFirstByProperty(DimensionAttribute.FIELD_ITEM, id);
            if (Objects.nonNull(attribute)) {
                // 当前科目已被使用,禁止删除!
                return OperateResult.operationFailure("subject_item_00001");
            }
            OrderDetail orderItem = orderItemService.findFirstByProperty(OrderDetail.FIELD_ITEM_CODE, entity.getCode());
            if (Objects.nonNull(orderItem)) {
                // 当前科目已被使用,禁止删除!
                return OperateResult.operationFailure("subject_item_00001");
            }

            getDao().delete(entity);
            return OperateResult.operationSuccess("core_service_00028");
        } else {
            return OperateResult.operationWarning("core_service_00029");
        }
    }

    /**
     * 根据预算主体查询私有预算主体科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<SubjectItem> findBySubject(String subjectId) {
        return dao.findListByProperty(SubjectItem.FIELD_SUBJECT_ID, subjectId);
    }

    /**
     * 根据预算主体查询私有预算主体科目(不包含冻结状态的)
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<SubjectItem> findBySubjectUnfrozen(String subjectId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(SubjectItem.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(SubjectItem.FROZEN, Boolean.FALSE));
        return findByFilters(search);
    }

    /**
     * 冻结/解冻预算类型
     *
     * @param ids 预算主体科目id
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> frozen(List<String> ids, boolean frozen) {
        List<SubjectItem> items = dao.findAllById(ids);
        for (SubjectItem item : items) {
            item.setFrozen(frozen);
        }
        this.save(items);
        return ResultData.success();
    }

    /**
     * 获取未分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    public PageResult<SubjectItem> getUnassigned(String subjectId, Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        List<SubjectItem> subjectItems = findBySubject(subjectId);
        if (CollectionUtils.isNotEmpty(subjectItems)) {
            search.addFilter(new SearchFilter(Item.CODE_FIELD, subjectItems.stream().map(SubjectItem::getCode).collect(Collectors.toSet()), SearchFilter.Operator.NOTIN));
        }

        PageResult<Item> itemPageResult = itemService.findByPage(search);
        PageResult<SubjectItem> pageResult = new PageResult<>(itemPageResult);
        List<Item> itemList = itemPageResult.getRows();
        List<SubjectItem> subjectItemList = itemList.stream().map(i -> {
            SubjectItem item1 = new SubjectItem();
            item1.setCode(i.getCode());
            item1.setName(i.getName());
            return item1;
        }).collect(Collectors.toList());
        pageResult.setRows(subjectItemList);
        return pageResult;
    }

    /**
     * 获取已分配的预算科目
     *
     * @return 子实体清单
     */
    public PageResult<SubjectItem> getAssigned(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        return findByPage(search);
    }

    /**
     * 为指定预算主体分配预算科目
     *
     * @param subjectId 预算主体id
     * @param itemCodes 科目代码
     * @return 分配结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> assigne(String subjectId, Set<String> itemCodes) {
        Subject subject = subjectService.findOne(subjectId);
        if (Objects.isNull(subject)) {
            // 预算类型已被使用,不允许修改
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }
        List<Item> itemList = itemService.getItems(itemCodes);
        List<SubjectItem> subjectItems = new ArrayList<>();
        SubjectItem subjectItem;
        for (Item item : itemList) {
            subjectItem = new SubjectItem();
            subjectItem.setSubjectId(subjectId);
            subjectItem.setCode(item.getCode());
            subjectItem.setName(item.getName());
            subjectItems.add(subjectItem);
        }
        this.save(subjectItems);
        return ResultData.success();
    }

    /**
     * 检查是否可以参考引用
     * 当主体不存在科目时才允许参考引用
     *
     * @param subjectId 预算主体id
     * @return 检查结果
     */
    public ResultData<Void> checkReference(String subjectId) {
        SubjectItem subjectItem = dao.findFirstByProperty(SubjectItem.FIELD_SUBJECT_ID, subjectId);
        if (Objects.isNull(subjectItem)) {
            return ResultData.success();
        } else {
            // 预算主体[{0}]已存在科目,不允许再参考引用!
            return ResultData.fail(ContextUtil.getMessage("subject_item_00002"));
        }
    }

    /**
     * 参考引用
     * 当主体不存在科目时才允许参考引用
     *
     * @param currentId   当前预算主体id
     * @param referenceId 参考预算主体id
     * @return 检查结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> reference(String currentId, String referenceId) {
        ResultData<Void> resultData = checkReference(currentId);
        if (resultData.failed()) {
            return resultData;
        }
        List<SubjectItem> subjectItems = findBySubject(referenceId);
        if (CollectionUtils.isEmpty(subjectItems)) {
            // 预算主体[{0}]还未维护科目!
            return ResultData.fail(ContextUtil.getMessage("subject_item_00003", referenceId));
        }
        SubjectItem subjectItem;
        List<SubjectItem> itemList = new ArrayList<>();
        for (SubjectItem item : subjectItems) {
            if (item.getFrozen()) {
                continue;
            }
            subjectItem = new SubjectItem();
            subjectItem.setSubjectId(currentId);
            subjectItem.setCode(item.getCode());
            subjectItem.setName(item.getName());
            subjectItem.setStrategyId(item.getStrategyId());
            subjectItem.setStrategyName(item.getStrategyName());
            itemList.add(subjectItem);
        }
        this.save(itemList);
        return ResultData.success();
    }
}