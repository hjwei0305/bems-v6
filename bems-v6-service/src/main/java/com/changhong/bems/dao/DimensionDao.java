package com.changhong.bems.dao;

import com.changhong.bems.entity.Dimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算维度(Dimension)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Repository
public interface DimensionDao extends BaseEntityDao<Dimension> {

}