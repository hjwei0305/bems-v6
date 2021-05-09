package com.changhong.bems.service;

import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.serial.sdk.SerialService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;


/**
 * 预算池(Pool)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Service
public class PoolService extends BaseEntityService<Pool> {
    private static final Logger LOG = LoggerFactory.getLogger(PoolService.class);
    @Autowired
    private PoolDao dao;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PoolAmountService poolAmountService;
    @Autowired
    private ExecutionRecordService executionRecordService;
    @Autowired(required = false)
    private SerialService serialService;

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
     * @param poolCode 预算池编码
     * @return 返回符合条件的预算池
     */
    public Pool getPoolByCode(String poolCode) {
        return dao.findByProperty(Pool.CODE_FIELD, poolCode);
    }

    /**
     * 创建一个预算池
     *
     * @param order     申请单
     * @param attribute 预算维度属性
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Pool> createPool(Order order, BaseAttribute attribute) {
        // 预算主体id
        String subjectId = order.getSubjectId();
        DimensionAttribute dimensionAttribute = new DimensionAttribute(attribute);
        dimensionAttribute.setSubjectId(order.getSubjectId());
        List<DimensionDto> dimensions = categoryService.getAssigned(order.getCategoryId());
        if (CollectionUtils.isEmpty(dimensions)) {
            // 预算类型[{0}]下未找到预算维度!
            return ResultData.fail(ContextUtil.getMessage("category_00007", order.getCategoryName()));
        }
        StringJoiner joiner = new StringJoiner(",");
        for (DimensionDto dimension : dimensions) {
            joiner.add(dimension.getCode());
        }
        dimensionAttribute.setAttribute(joiner.toString());
        ResultData<String> resultData = dimensionAttributeService.add(dimensionAttribute);
        if (resultData.failed()) {
            return ResultData.fail(resultData.getMessage());
        }
        // 属性id
        String attributeId = resultData.getData();
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_ID, attributeId));
        Pool pool = dao.findOneByFilters(search);
        if (Objects.isNull(pool)) {
            pool = new Pool();
            // 预算池编码
            pool.setCode(serialService.getNumber(Pool.class, ContextUtil.getTenantCode()));
            // 预算主体
            pool.setSubjectId(subjectId);
            // 属性id
            pool.setAttributeId(attributeId);
            // 币种
            pool.setCurrencyCode(order.getCurrencyCode());
            pool.setCurrencyName(order.getCurrencyName());
            // 归口管理部门
            pool.setManageOrg(order.getManagerOrgCode());
            pool.setManageOrgName(order.getManagerOrgName());
            // 期间类型
            pool.setPeriodType(order.getPeriodType());
            Period period = periodService.findOne(attribute.getPeriod());
            if (Objects.isNull(period)) {
                // 预算期间不存在
                return ResultData.fail(ContextUtil.getMessage("period_00002"));
            }
            pool.setStartDate(period.getStartDate());
            pool.setEndDate(period.getEndDate());
            Category category = categoryService.findOne(order.getCategoryId());
            if (Objects.isNull(category)) {
                // 预算类型不存在
                return ResultData.fail(ContextUtil.getMessage("category_00004", order.getCategoryId()));
            }
            pool.setUse(category.getUse());
            pool.setRoll(category.getRoll());

            OperateResultWithData<Pool> result = this.save(pool);
            if (result.notSuccessful()) {
                return ResultData.fail(result.getMessage());
            }
        }
        return ResultData.success(pool);
    }

    /**
     * 获取预算池当前可用余额
     *
     * @param poolId 预算池id
     */
    public double getPoolBalanceById(String poolId) {
        return poolAmountService.getPoolBalanceByPoolId(poolId);
    }

    /**
     * 获取预算池当前可用余额
     *
     * @param poolCode 预算池编码
     */
    public double getPoolBalanceByCode(String poolCode) {
        return poolAmountService.getPoolBalanceByPoolCode(poolCode);
    }

    /**
     * 获取预算池当前可用余额
     *
     * @param pool 预算池
     */
    public double getPoolBalance(Pool pool) {
        if (Objects.isNull(pool)) {
            // 未找到预算池
            throw new ServiceException(ContextUtil.getMessage("pool_00001"));
        }
        // 实时计算当前预算池可用金额
        double amount = poolAmountService.getPoolBalanceByPoolCode(pool.getCode());
        pool.setBalance(amount);
        return amount;
    }

    /**
     * @param record 执行记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLog(ExecutionRecord record) {
        Pool pool = dao.findByProperty(Pool.CODE_FIELD, record.getPoolCode());
        if (Objects.isNull(pool)) {
            LOG.error("预算池[" + record.getPoolCode() + "]不存在");
            return;
        }
        // 操作时间
        record.setOpTime(LocalDateTime.now());
        // 操作人
        if (StringUtils.isBlank(record.getOpUserAccount())) {
            SessionUser user = ContextUtil.getSessionUser();
            record.setOpUserAccount(user.getAccount());
            record.setOpUserName(user.getUserName());
        }
        // 设置预算属性id
        record.setAttributeId(pool.getAttributeId());
        // 记录执行日志
        executionRecordService.save(record);
        // 累计金额
        poolAmountService.countAmount(pool, record.getOperation(), record.getAmount());
    }
}