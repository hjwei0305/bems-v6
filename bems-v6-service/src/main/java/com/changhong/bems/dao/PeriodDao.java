package com.changhong.bems.dao;

import com.changhong.bems.entity.Period;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算期间(Period)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@Repository
public interface PeriodDao extends BaseEntityDao<Period> {

}