package com.changhong.bems.dao;

import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 预算申请单(Order)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Repository
public interface OrderDao extends BaseEntityDao<Order> {

    /**
     * 更新订单申请金额
     *
     * @param id     订单id
     * @param amount 金额
     */
    @Modifying
    @Query("update Order o set o.applyAmount = :amount where o.id = :id ")
    void updateAmount(@Param("id") String id, @Param("amount") double amount);
}