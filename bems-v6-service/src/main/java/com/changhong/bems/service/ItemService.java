package com.changhong.bems.service;

import com.changhong.bems.dao.ItemDao;
import com.changhong.bems.entity.Item;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    @Override
    protected BaseEntityDao<Item> getDao() {
        return dao;
    }

}