package com.changhong.bems.service;

import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
    private SubjectItemService subjectItemService;

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
        Item item = dao.findOne(id);
        if (Objects.nonNull(item)) {
            SubjectItem subjectItem = subjectItemService.findFirstByProperty(SubjectItem.FIELD_CODE, item.getCode());
            if (Objects.nonNull(subjectItem)) {
                // 当前科目已被使用,禁止删除!
                return OperateResult.operationFailure("item_00001");
            }
            return OperateResult.operationSuccess();
        } else {
            // 预算科目不存在!
            return OperateResult.operationFailure("item_00002");
        }
    }

    /**
     * 根据code获取预算科目
     */
    public List<Item> getItems(Collection<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        return dao.findByFilter(new SearchFilter(Item.CODE_FIELD, codes, SearchFilter.Operator.IN));
    }
}