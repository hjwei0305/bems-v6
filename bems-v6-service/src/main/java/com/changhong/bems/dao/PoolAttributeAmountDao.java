package com.changhong.bems.dao;

import com.changhong.bems.entity.PoolAttributeAmount;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算池维度属性金额(PoolAttributeAmount)数据库访问类
 *
 * @author sei
 * @since 2021-09-30 10:27:03
 */
@Repository
public interface PoolAttributeAmountDao extends BaseEntityDao<PoolAttributeAmount>, PoolAttributeAmountExtDao {

}