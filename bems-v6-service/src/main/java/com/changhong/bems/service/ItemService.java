package com.changhong.bems.service;

import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Item;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private DimensionAttributeService dimensionAttributeService;

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
        DimensionAttribute attribute = dimensionAttributeService.findFirstByProperty(DimensionAttribute.FIELD_ITEM, id);
        if (Objects.nonNull(attribute)) {
            // 当前科目已被使用,禁止删除!
            return OperateResult.operationFailure("item_00001", attribute);
        }
        return OperateResult.operationSuccess();
    }
}