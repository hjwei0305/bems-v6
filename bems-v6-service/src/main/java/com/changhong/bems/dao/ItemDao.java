package com.changhong.bems.dao;

import com.changhong.bems.entity.Item;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算科目(Item)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Repository
public interface ItemDao extends BaseEntityDao<Item> {

}