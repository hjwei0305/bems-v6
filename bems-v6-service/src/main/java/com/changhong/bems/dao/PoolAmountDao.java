package com.changhong.bems.dao;

import com.changhong.bems.entity.PoolAmount;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算池金额(PoolAmount)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:14:01
 */
@Repository
public interface PoolAmountDao extends BaseEntityDao<PoolAmount> {

}