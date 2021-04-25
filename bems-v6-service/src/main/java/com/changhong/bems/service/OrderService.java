package com.changhong.bems.service;

import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
    @Autowired
    private OrderDao dao;

    @Override
    protected BaseEntityDao<Order> getDao() {
        return dao;
    }

}