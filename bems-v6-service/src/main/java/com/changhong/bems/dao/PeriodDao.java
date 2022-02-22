package com.changhong.bems.dao;

import com.changhong.bems.entity.Period;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
     * @param id 要关闭的预算期间id
     * @return 更新个数
     */
    @Modifying
    @Query("update Period p set p.closed = :status where p.id = :id")
    int updateCloseStatus(@Param("id") String id, @Param("status") boolean status);

    /**
     * 关闭过期预算期间
     *
     * @param endDate 截止时间
     */
    @Modifying
    @Query("update Period p set p.closed = true where p.closed = false and p.endDate < :endDate")
    int closingOverduePeriod(@Param("endDate") LocalDate endDate);

    @Query("select distinct p.type from Period p where p.subjectId = :subjectId and p.year = :year and p.closed = false ")
    Set<Object> getEnabledPeriodType(@Param("subjectId") String subjectId, @Param("year") int year);
}