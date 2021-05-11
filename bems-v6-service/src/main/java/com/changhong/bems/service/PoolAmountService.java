package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAmountDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.PoolAmount;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.util.ArithUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


/**
 * 预算池金额(PoolAmount)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:14:01
 */
@Service
public class PoolAmountService extends BaseEntityService<PoolAmount> {
    @Autowired
    private PoolAmountDao dao;

    @Override
    protected BaseEntityDao<PoolAmount> getDao() {
        return dao;
    }

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolId 预算池id
     * @return 当前预算池可用余额
     */
    public double getPoolBalanceByPoolId(String poolId) {
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_ID, poolId);
        return this.getPoolBalance(amounts);
    }

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolCode 预算池编码
     * @return 当前预算池可用余额
     */
    public double getPoolBalanceByPoolCode(String poolCode) {
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_CODE, poolCode);
        return this.getPoolBalance(amounts);
    }

    /**
     * 按类型累计金额
     *
     * @param pool      预算池
     * @param operation 操作类型
     * @param amount    本次发生金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void countAmount(Pool pool, OperationType operation, double amount) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(PoolAmount.FIELD_POOL_ID, pool.getId()));
        search.addFilter(new SearchFilter(PoolAmount.FIELD_OPERATION, operation));
        PoolAmount poolAmount = dao.findOneByFilters(search);
        if (Objects.isNull(poolAmount)) {
            poolAmount = new PoolAmount();
            poolAmount.setPoolId(pool.getId());
            poolAmount.setPoolCode(pool.getCode());
            poolAmount.setOperation(operation);
        }
        poolAmount.setAmount(ArithUtils.add(poolAmount.getAmount(), amount));
        this.save(poolAmount);
    }

    /**
     * 计算预算池当前余额
     * 公式: 注入金额+小于0的预注入金额-使用金额
     *
     * @param amounts 预算池金额
     * @return 当前余额
     */
    private double getPoolBalance(List<PoolAmount> amounts) {
        double balance = 0;
        if (CollectionUtils.isNotEmpty(amounts)) {
            // 注入金额+小于0的预注入金额-使用金额
            for (PoolAmount amount : amounts) {
                switch (amount.getOperation()) {
                    case RELEASE:
                        balance = ArithUtils.add(balance, amount.getAmount());
                        break;
                    case USE:
                        balance = ArithUtils.sub(balance, amount.getAmount());
                        break;
                    default:
                }
            }
        }
        return balance;
    }
}