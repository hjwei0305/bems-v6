package com.changhong.bems.dao;

import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算申请单(Order)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Repository
public interface OrderDao extends BaseEntityDao<Order> {

}