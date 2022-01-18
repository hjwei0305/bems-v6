package com.changhong.bems.dao;

import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 预算行项(OrderDetail)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Repository
public interface OrderDetailDao extends BaseEntityDao<OrderDetail>, OrderDetailExtDao {

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 返回操作记录数
     */
    @Modifying
    @Query("delete from OrderDetail d where d.orderId = :orderId")
    int clearOrderItems(@Param("orderId") String orderId);

    /**
     * 预算分解按源预算池代码删除
     *
     * @param orderId 分解分组行项Id
     */
    @Modifying
    @Query("delete from OrderDetail d where d.orderId = :orderId and d.originPoolCode = :originPoolCode")
    int removeSplitOrderItems(@Param("orderId") String orderId, @Param("originPoolCode") String originPoolCode);

    /**
     * 获取订单总金额
     *
     * @param orderId 订单头id
     * @return 返回总金额
     */
    @Query("select sum(d.amount) from OrderDetail d where d.orderId = :orderId")
    double getSumAmount(@Param("orderId") String orderId);

    /**
     * 获取申请单调整金额
     *
     * @param orderId 申请单号
     * @return 返回调整金额:调增金额,调减金额
     */
    @Query("select (select sum(d.amount) from OrderDetail d where d.orderId = :orderId and d.amount>0) as increaseNum, (select sum(d.amount) from OrderDetail d where d.orderId = :orderId and d.amount<0) as decreaseNum from Order where id=:orderId")
    Object[] getAdjustData(@Param("orderId") String orderId);

    /**
     * 检查行项是否有错误未处理
     *
     * @param orderId 订单头id
     * @return 错误数
     */
    @Query("select count(d.id) from OrderDetail d where d.orderId = :orderId and d.hasErr = true ")
    long getHasErrCount(@Param("orderId") String orderId);

    /**
     * 按订单id设置所有行项的处理状态为处理中
     * @param orderId 订单头id
     * @return
     */
    @Modifying
    @Query("update OrderDetail d set d.processing = true where d.orderId = :orderId")
    int setProcessing4All(@Param("orderId") String orderId);

    /**
     * 更新行项的处理状态为处理完成
     * @param detailId 订单行项id
     * @return
     */
    @Modifying
    @Query("update OrderDetail d set d.processing = false where d.id = :detailId")
    int setProcessed(@Param("detailId") String detailId);

    /**
     * 检查行项是否有处理中的行项
     *
     * @param orderId 订单头id
     * @return 处理中的行项数
     */
    @Query("select count(d.id) from OrderDetail d where d.orderId = :orderId and d.processing = true ")
    long getProcessingCount(@Param("orderId") String orderId);
}