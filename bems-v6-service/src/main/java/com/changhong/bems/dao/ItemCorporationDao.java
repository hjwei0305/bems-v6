package com.changhong.bems.dao;

import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.ItemCorporation;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 公司预算科目(ItemCorporation)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Repository
public interface ItemCorporationDao extends BaseEntityDao<ItemCorporation> {

    @Modifying
    void deleteByItemId(String itemId);

    @Modifying
    void deleteByItemIdIn(Set<String> itemIds);

}