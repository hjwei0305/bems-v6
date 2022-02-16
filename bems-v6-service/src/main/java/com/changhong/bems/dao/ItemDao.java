package com.changhong.bems.dao;

import com.changhong.bems.entity.Item;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 预算科目(Item)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Repository
public interface ItemDao extends BaseEntityDao<Item> {

    @Modifying
    @Query("update Item i set i.frozen = :disabled where i.id in :ids ")
    int disabledGeneral(@Param("ids") Set<String> ids, @Param("disabled") boolean disabled);
}