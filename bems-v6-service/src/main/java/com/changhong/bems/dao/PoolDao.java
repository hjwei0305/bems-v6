package com.changhong.bems.dao;

import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 预算池(Pool)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Repository
public interface PoolDao extends BaseEntityDao<Pool> {

    /**
     * 更新预算池余额
     *
     * @param id      订单id
     * @param balance 金额
     */
    @Modifying
    @Query("update Pool p set p.balance = :balance where p.id = :id ")
    void updateAmount(@Param("id") String id, @Param("balance") double balance);

    /**
     * 通过Id更新预算池启用禁用状态
     *
     * @param ids     预算池Id集合
     * @param actived 启用禁用状态
     * @return 更新记录数
     */
    @Modifying
    @Query("update Pool p set p.actived = :actived where p.id in :ids ")
    int updateActiveStatus(Set<String> ids, boolean actived);
}