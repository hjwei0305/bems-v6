package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectItemDao;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        DimensionAttribute attribute = dimensionAttributeService.findFirstByProperty(DimensionAttribute.FIELD_ITEM, id);
        if (Objects.nonNull(attribute)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001");
        }
        OrderDetail orderItem = orderItemService.findFirstByProperty(OrderDetail.FIELD_ITEM_ID, id);
        if (Objects.nonNull(orderItem)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001");
        }
        // TODO 导入明细
        return OperateResult.operationSuccess();
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
            SearchFilter searchCode = search.getFilters().stream().filter(f -> StringUtils.equals(f.getFieldName(), Item.CODE_FIELD)).findAny().orElse(null);
            if (Objects.nonNull(searchCode)) {
                if (subjectItems.stream().anyMatch(i -> StringUtils.containsIgnoreCase(i.getCode(), String.valueOf(searchCode.getValue())))) {
                    // todo
                }
            } else {
                search.addFilter(new SearchFilter(Item.CODE_FIELD, subjectItems.stream().map(SubjectItem::getCode).collect(Collectors.toSet()), SearchFilter.Operator.NOTIN));
            }
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
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    public PageResult<SubjectItem> getAssigned(String subjectId, Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        search.addFilter(new SearchFilter(SubjectItem.FIELD_SUBJECT_ID, subjectId));
        return findByPage(search);
    }

    /**
     * 为指定预算主体分配预算科目
     *
     * @param subjectId 预算主体id
     * @param itemCodes 科目代码
     * @return 分配结果
     */
    public ResultData<Void> assigne(String subjectId, Set<String> itemCodes) {
        return null;
    }

    /**
     * 解除预算主体与科目分配关系
     *
     * @param subjectId 预算主体id
     * @param itemCodes 科目代码
     * @return 分配结果
     */
    public ResultData<Void> unassigne(String subjectId, Set<String> itemCodes) {
        return null;
    }
}