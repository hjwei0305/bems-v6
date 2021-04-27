package com.changhong.bems.service;

import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预算维度属性(OrderDetail)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Service
public class OrderDetailService extends BaseEntityService<OrderDetail> {
    @Autowired
    private OrderDetailDao dao;

    @Override
    protected BaseEntityDao<OrderDetail> getDao() {
        return dao;
    }

}