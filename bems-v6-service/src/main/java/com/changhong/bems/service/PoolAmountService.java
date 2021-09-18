package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAmountDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.PoolAmount;
import com.changhong.bems.dto.PoolAmountQuotaDto;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.util.ArithUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 预算池金额(PoolAmount)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:14:01
 */
@Service
public class PoolAmountService {
    @Autowired
    private PoolAmountDao dao;

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolId 预算池id
     * @return 当前预算池可用余额
     */
    public BigDecimal getPoolBalanceByPoolId(String poolId) {
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_ID, poolId);
        PoolAmountQuotaDto quota = this.getPoolAmountQuota(amounts);
        return quota.getBalance();
    }

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolCode 预算池编码
     * @return 当前预算池可用余额
     */
    public BigDecimal getPoolBalanceByPoolCode(String poolCode) {
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_CODE, poolCode);
        PoolAmountQuotaDto quota = this.getPoolAmountQuota(amounts);
        return quota.getBalance();
    }

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolCode 预算池编码
     * @return 当前预算池可用余额
     */
    public PoolAmountQuotaDto getPoolAmountQuota(String poolCode) {
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_CODE, poolCode);
        return this.getPoolAmountQuota(amounts);
    }

    /**
     * 按类型累计金额
     *
     * @param pool      预算池
     * @param operation 操作类型
     * @param operation 操作类型
     * @param amount    本次发生金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void countAmount(Pool pool, boolean internal, OperationType operation, BigDecimal amount) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(PoolAmount.FIELD_POOL_ID, pool.getId()));
        search.addFilter(new SearchFilter(PoolAmount.FIELD_INTERNAL, internal));
        search.addFilter(new SearchFilter(PoolAmount.FIELD_OPERATION, operation));
        PoolAmount poolAmount = dao.findOneByFilters(search);
        if (Objects.isNull(poolAmount)) {
            poolAmount = new PoolAmount();
            poolAmount.setPoolId(pool.getId());
            poolAmount.setPoolCode(pool.getCode());
            poolAmount.setInternal(internal);
            poolAmount.setOperation(operation);
        }
        poolAmount.setAmount(ArithUtils.add(poolAmount.getAmount(), amount.doubleValue()));
        dao.save(poolAmount);
    }

    /**
     * 计算预算池当前余额
     * 公式: 注入金额+释放(使用)金额-使用金额
     *
     * @param amounts 预算池金额
     * @return 当前余额
     */
    private PoolAmountQuotaDto getPoolAmountQuota(List<PoolAmount> amounts) {
        PoolAmountQuotaDto quota = new PoolAmountQuotaDto();
        //BigDecimal balance = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(amounts)) {
            String poolCode = amounts.get(0).getPoolCode();
            // 注入金额 + 释放(使用)金额 - 使用金额
            for (PoolAmount amount : amounts) {
                if (!StringUtils.equals(poolCode, amount.getPoolCode())) {
                    // 预算池金额计算异常:不是同一个预算池
                    throw new ServiceException(ContextUtil.getMessage("pool_00024"));
                }
                switch (amount.getOperation()) {
                    case RELEASE:
                        // 注入下达
                    case FREED:
                        // 释放
                        // balance = ArithUtils.add(balance, amount.getAmount().doubleValue());
                        quota.addTotalAmount(amount.getAmount());
                        break;
                    case USE:
                        // 使用
                        // balance = ArithUtils.sub(balance, amount.getAmount().doubleValue());
                        quota.addUseAmount(amount.getAmount());
                        break;
                    default:
                }
            }
            quota.setPoolCode(poolCode);
        }
        // return balance;
        return quota;
    }
}