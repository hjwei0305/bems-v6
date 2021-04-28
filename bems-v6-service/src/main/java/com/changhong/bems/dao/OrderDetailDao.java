package com.changhong.bems.dao;

import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 预算行项(OrderDetail)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Repository
public interface OrderDetailDao extends BaseEntityDao<OrderDetail> {


    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @Modifying
    @Query("delete from OrderDetail d where d.orderId = :orderId")
    int clearOrderItems(@Param("orderId") String orderId);
}