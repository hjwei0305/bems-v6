package com.changhong.bems.dao;

import com.changhong.bems.entity.BudgetType;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算类型(Category)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Repository
public interface BudgetTypeDao extends BaseEntityDao<BudgetType> {

}