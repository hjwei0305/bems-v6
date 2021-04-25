package com.changhong.bems.dao;

import com.changhong.bems.entity.OrderItem;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算维度属性(OrderItem)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Repository
public interface OrderItemDao extends BaseEntityDao<OrderItem> {

}