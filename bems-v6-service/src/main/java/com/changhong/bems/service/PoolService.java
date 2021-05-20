package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAttributeViewDao;
import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.serial.sdk.SerialService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;


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
    private PoolAttributeViewDao poolAttributeDao;
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
     * @param attributeCode 预算维度hash
     * @return 返回满足条件的预算池
     */
    public ResultData<Pool> getPool(String subjectId, long attributeCode) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCode));
        Pool pool = dao.findFirstByFilters(search);
        if (Objects.nonNull(pool)) {
            return ResultData.success(pool);
        } else {
            // 预算池不存在
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    /**
     * 创建一个预算池
     *
     * @param order         申请单
     * @param baseAttribute 预算维度属性
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Pool> createPool(Order order, BaseAttribute baseAttribute) {
        if (Objects.isNull(order)) {
            // 创建预算池时,订单不能为空!
            return ResultData.fail(ContextUtil.getMessage("pool_00005"));
        }
        // 预算主体id
        String subjectId = order.getSubjectId();
        if (StringUtils.isBlank(subjectId)) {
            // 创建预算池时,预算主体不能为空!
            return ResultData.fail(ContextUtil.getMessage("pool_00008"));
        }

        DimensionAttribute attribute = dimensionAttributeService.getAttribute(subjectId, baseAttribute.getAttributeCode());
        if (Objects.isNull(attribute)) {
            ResultData<DimensionAttribute> resultData = dimensionAttributeService.createAttribute(subjectId, order.getCategoryId(), baseAttribute);
            if (resultData.failed()) {
                return ResultData.fail(resultData.getMessage());
            }
            attribute = resultData.getData();
        }

        Long attributeCode = attribute.getAttributeCode();
        String periodId = attribute.getPeriod();
        if (StringUtils.isBlank(periodId)) {
            // 创建预算池时,期间不能为空!
            return ResultData.fail(ContextUtil.getMessage("pool_00006"));
        }

        // 属性id
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCode));
        Pool pool = dao.findOneByFilters(search);
        if (Objects.isNull(pool)) {
            pool = new Pool();
            // 预算池编码
            pool.setCode(serialService.getNumber(Pool.class, ContextUtil.getTenantCode()));
            // 预算主体
            pool.setSubjectId(subjectId);
            // 属性id
            pool.setAttributeCode(attributeCode);
            // 币种
            pool.setCurrencyCode(order.getCurrencyCode());
            pool.setCurrencyName(order.getCurrencyName());
            // 归口管理部门
            pool.setManageOrg(order.getManagerOrgCode());
            pool.setManageOrgName(order.getManagerOrgName());
            // 期间类型
            pool.setPeriodType(order.getPeriodType());
            Period period = periodService.findOne(periodId);
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
        // 操作时间
        record.setOpTime(LocalDateTime.now());
        // 操作人
        if (StringUtils.isBlank(record.getOpUserAccount())) {
            SessionUser user = ContextUtil.getSessionUser();
            record.setOpUserAccount(user.getAccount());
            record.setOpUserName(user.getUserName());
        }
        // 记录执行日志
        executionRecordService.save(record);

        // 检查当前记录是否影响预算池余额
        if (record.getIsPoolAmount()) {
            /*
             在注入或分解是可能还没有预算池,此时只记录日志.
             注入或分解为负数的,必须存在预算池,提交流程时做预占用处理
             */
            if (StringUtils.isNotBlank(record.getSubjectId())) {
                ResultData<Pool> poolResult = this.getPool(record.getSubjectId(), record.getAttributeCode());
                if (poolResult.successful()) {
                    Pool pool = poolResult.getData();
                    // 累计金额
                    poolAmountService.countAmount(pool, record.getOperation(), record.getAmount());
                    // 实时计算当前预算池可用金额
                    double amount = poolAmountService.getPoolBalanceByPoolCode(pool.getCode());
                    // 更新预算池金额
                    dao.updateAmount(pool.getId(), amount);
                    return;
                }
            }
            LOG.error("预算池[" + record.getPoolCode() + "]不存在");
        }
    }

    /**
     * 分页查询预算池
     *
     * @param search 查询对象
     * @return 分页结果
     */
    public PageResult<PoolAttributeView> findPoolByPage(Search search) {
        return poolAttributeDao.findByPage(search);
    }

    /**
     * 按预算池id获取预算池
     *
     * @param id 预算池id
     * @return 预算池
     */
    public PoolAttributeView findPoolAttribute(String id) {
        return poolAttributeDao.findOne(id);
    }

    /**
     * 按预算主体和代码查询预算池
     *
     * @param subjectId 预算主体id
     * @param code      预算编码
     * @return 预算池
     */
    public PoolAttributeView findPoolAttribute(String subjectId, String code) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_CODE, code));
        return poolAttributeDao.findFirstByFilters(search);
    }

    /**
     * 通过Id启用预算池
     *
     * @param ids 预算池Id集合
     * @return 启用结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> updateActiveStatus(Set<String> ids, boolean isActive) {
        dao.updateActiveStatus(ids, isActive);
        return ResultData.success();
    }

    /**
     * 分页查询预算执行日志
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    public PageResult<ExecutionRecordView> findRecordByPage(Search search) {
        return executionRecordService.findViewByPage(search);
    }
}