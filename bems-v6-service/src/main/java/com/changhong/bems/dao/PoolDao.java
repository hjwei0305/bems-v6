package com.changhong.bems.dao;

import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 预算池(Pool)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Repository
public interface PoolDao extends BaseEntityDao<Pool>, PoolExtDao {

    /**
     * 更新预算池余额
     *
     * @param id      订单id
     * @param balance 金额
     */
    @Modifying
    @Query("update Pool p set p.totalAmount = :total, p.usedAmount = :used, p.balance = :balance where p.id = :id ")
    void updateAmount(@Param("id") String id, @Param("total") BigDecimal total,
                      @Param("used") BigDecimal used, @Param("balance") BigDecimal balance);

    /**
     * 通过Id更新预算池启用禁用状态
     *
     * @param id      预算池ID
     * @param actived 启用禁用状态
     */
    @Modifying
    @Query("update Pool p set p.actived = :actived where p.id = :id ")
    void updateActiveStatus(@Param("id") String id, @Param("actived") boolean actived);

    /**
     * 获取超过指定日期的非自定义期间类型预算池
     *
     * @param year 所属年度
     * @param date 当前日期
     * @return 返回满足条件的预算池
     */
    @Query("select p from Pool p where p.year = :year and p.endDate <= :date and p.periodType <> 'CUSTOMIZE' and p.balance <> 0")
    List<Pool> findNoCustomizeExpirePools(@Param("year") int year, @Param("date") LocalDate date);
}