package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAmountDao;
import com.changhong.bems.dao.PoolAttributeAmountDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.PoolAmountQuotaDto;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.PoolAmount;
import com.changhong.bems.entity.PoolAttributeAmount;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
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

    @Autowired
    private PoolAttributeAmountDao poolAttributeAmountDao;

    /**
     * 按预算池id查询预算池当前余额
     *
     * @param poolCode 预算池编码
     * @return 当前预算池可用余额
     */
    public BigDecimal getPoolBalanceByPoolCode(String poolCode) {
        PoolAmountQuotaDto quota = this.getPoolAmountQuota(poolCode);
        return quota.getBalance();
    }

    /**
     * 按类型累计金额
     *
     * @param pool      预算池
     * @param internal  是否是预算内部操作
     * @param operation 操作类型
     * @param amount    本次发生金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void countAmount(Pool pool, boolean internal, OperationType operation, BigDecimal amount) {
        String poolId = pool.getId();
        String poolCode = pool.getCode();
        String tenantCode = ContextUtil.getTenantCode();
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(PoolAmount.FIELD_POOL_ID, poolId));
        search.addFilter(new SearchFilter(PoolAmount.FIELD_OPERATION, operation));
        search.addFilter(new SearchFilter(PoolAmount.FIELD_INTERNAL, internal));
        PoolAmount poolAmount = dao.findOneByFilters(search);
        if (Objects.isNull(poolAmount)) {
            poolAmount = new PoolAmount();
            poolAmount.setTenantCode(tenantCode);
            poolAmount.setPoolId(poolId);
            poolAmount.setPoolCode(poolCode);
            poolAmount.setInternal(internal);
            poolAmount.setOperation(operation);
        }
        poolAmount.setAmount(poolAmount.getAmount().add(amount));
        dao.save(poolAmount);

        PoolAttributeAmount attributeAmount = poolAttributeAmountDao.findByProperty(PoolAttributeAmount.FIELD_POOL_ID, poolId);
        if (Objects.isNull(attributeAmount)) {
            attributeAmount = new PoolAttributeAmount();
            attributeAmount.setPoolId(poolId);
            attributeAmount.setSubjectId(pool.getSubjectId());
            attributeAmount.setTenantCode(tenantCode);
        }
        if (internal) {
            // 预算内部调整或分解
            switch (operation) {
                case RELEASE:
                    // 调入
                    attributeAmount.setReviseInAmount(attributeAmount.getReviseInAmount().add(amount));
                    break;
                case USE:
                    // 调出
                    attributeAmount.setReviseOutAmount(attributeAmount.getReviseOutAmount().add(amount));
                    break;
                case FREED:
                    attributeAmount.setReviseOutAmount(attributeAmount.getReviseOutAmount().subtract(amount));
                    break;
                default:
            }
        } else {
            // 预算外部注入或使用
            switch (operation) {
                case RELEASE:
                    // 外部注入
                    attributeAmount.setInjectAmount(attributeAmount.getInjectAmount().add(amount));
                    break;
                case USE:
                    attributeAmount.setUsedAmount(attributeAmount.getUsedAmount().add(amount));
                    break;
                case FREED:
                    attributeAmount.setUsedAmount(attributeAmount.getUsedAmount().subtract(amount));
                    break;
                default:
            }
        }
        poolAttributeAmountDao.save(attributeAmount);
    }

    /**
     * 计算预算池金额额度
     * 公式:
     * 注入总额=注入金额之和
     * 使用总额=使用金额之和
     * 可用余额=注入金额-(使用金额-释放(使用)金额)
     * <p>
     * 内部调整:A预算到B预算
     * 内部分解:年度到月度
     * 内部结转:1月到2月
     * 内部使用:A预算扣减,年度预算扣减,1月预算扣减,
     * <p>
     * 外部注入:下达全新预算
     * 外部使用:业务系统使用
     *
     * @param poolCode 预算池代码
     * @return 当前余额
     */
    public PoolAmountQuotaDto getPoolAmountQuota(String poolCode) {
        PoolAmountQuotaDto quota = new PoolAmountQuotaDto(poolCode);
        List<PoolAmount> amounts = dao.findListByProperty(PoolAmount.FIELD_POOL_CODE, poolCode);
        if (CollectionUtils.isNotEmpty(amounts)) {
            // 注入金额 + 释放(使用)金额 - 使用金额
            for (PoolAmount amount : amounts) {
                switch (amount.getOperation()) {
                    case RELEASE:
                        // 注入下达
                        quota.addTotalAmount(amount.getAmount());
                        break;
                    case USE:
                        // 使用
                        quota.addUseAmount(amount.getAmount());
                        break;
                    case FREED:
                        // 释放.减去释放金额(加上取负的释放金额)
                        quota.addUseAmount(amount.getAmount().negate());
                        break;
                    default:
                }
            }
        }
        return quota;
    }
}