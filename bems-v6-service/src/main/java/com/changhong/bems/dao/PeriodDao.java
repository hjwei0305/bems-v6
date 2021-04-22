package com.changhong.bems.dao;

import com.changhong.bems.entity.Period;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预算期间(Period)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@Repository
public interface PeriodDao extends BaseEntityDao<Period> {

    /**
     * 关闭预算期间
     *
     * @param ids 要关闭的预算期间id
     * @return 更新个数
     */
    @Modifying
    @Query("update Period p set p.closed = true where p.id in :ids")
    int closedPeriod(@Param("ids") List<String> ids);
}