package com.changhong.bems.service;

import com.changhong.bems.dao.OrderItemDao;
import com.changhong.bems.entity.OrderItem;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算维度属性(OrderItem)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Service
public class OrderItemService extends BaseEntityService<OrderItem> {
    @Autowired
    private OrderItemDao dao;

    @Override
    protected BaseEntityDao<OrderItem> getDao() {
        return dao;
    }

}