package com.changhong.bems.service;

import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


/**
 * 预算池(Pool)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Service
public class PoolService extends BaseEntityService<Pool> {
    @Autowired
    private PoolDao dao;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;

    @Override
    protected BaseEntityDao<Pool> getDao() {
        return dao;
    }

    /**
     * 按预算主体和属性hash获取预算池
     *
     * @param subjectId     预算主体id
     * @param attributeHash 预算维度hash
     * @return 返回满足条件的预算池
     */
    public ResultData<Pool> getPool(String subjectId, long attributeHash) {
        DimensionAttribute attribute = dimensionAttributeService.getAttribute(subjectId, attributeHash);
        if (Objects.isNull(attribute)) {
            // 预算池不存在
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
        Pool pool = this.getPool(subjectId, attribute.getId());
        if (Objects.isNull(pool)) {
            return ResultData.success(pool);
        } else {
            // 预算池不存在
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    /**
     * 按主体和属性id获取预算池
     *
     * @param subjectId   预算主体id
     * @param attributeId 预算维度属性id
     * @return 返回符合条件的预算池
     */
    public Pool getPool(String subjectId, String attributeId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_ID, attributeId));
        // 非禁用的预算池
        search.addFilter(new SearchFilter(Pool.FIELD_ACTIVED, Boolean.TRUE));
        return dao.findFirstByFilters(search);
    }

    /**
     * 按预算池编码获取预算池
     *
     * @param poolCode   预算池编码
     * @return 返回符合条件的预算池
     */
    public Pool getPoolByCode(String poolCode) {
        return dao.findByProperty(Pool.CODE_FIELD, poolCode);
    }

    /**
     * 创建一个预算池
     *
     * @param order  申请单
     * @param detail 申请单行项
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPool(Order order, OrderDetail detail) {

    }

    /**
     * 获取预算池当前可用余额
     *
     * @param poolId 预算池id
     */
    public double getPoolAmount(String poolId) {
        Pool pool = dao.findOne(poolId);
        return getPoolAmount(pool);
    }

    /**
     * 获取预算池当前可用余额
     *
     * @param pool 预算池
     */
    public double getPoolAmount(Pool pool) {
        if (Objects.isNull(pool)) {
            // 未找到预算池
            throw new ServiceException(ContextUtil.getMessage("pool_00001"));
        }
        // todo 实时计算当前预算池可用金额
        double amount = 0;
        pool.setBalance(amount);
        return amount;
    }
}