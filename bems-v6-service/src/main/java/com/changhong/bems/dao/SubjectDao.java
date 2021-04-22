package com.changhong.bems.dao;

import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算主体(Subject)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@Repository
public interface SubjectDao extends BaseEntityDao<Subject> {

}