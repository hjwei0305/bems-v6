package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.bems.entity.Dimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


/**
 * 预算维度(Dimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Service
public class DimensionService extends BaseEntityService<Dimension> {
    @Autowired
    private DimensionDao dao;

    @Override
    protected BaseEntityDao<Dimension> getDao() {
        return dao;
    }

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    public Set<KeyValueDto> findAllCodes() {
        Set<KeyValueDto> set = new HashSet<>();
        set.add(new KeyValueDto("period", "预算期间"));
        set.add(new KeyValueDto("item", "预算科目"));
        set.add(new KeyValueDto("org", "组织机构"));
        set.add(new KeyValueDto("project", "项目"));
        set.add(new KeyValueDto("udf1", "自定义1"));
        set.add(new KeyValueDto("udf2", "自定义2"));
        set.add(new KeyValueDto("udf3", "自定义3"));
        set.add(new KeyValueDto("udf4", "自定义4"));
        set.add(new KeyValueDto("udf5", "自定义5"));
        return set;
    }
}