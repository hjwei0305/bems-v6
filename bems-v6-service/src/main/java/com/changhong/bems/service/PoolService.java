package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.PoolAttributeViewDao;
import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.PoolAmountQuotaDto;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.LocalMockUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.limiter.support.lock.SeiLock;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.serial.sdk.SerialService;
import com.changhong.sei.util.DateUtils;
import com.changhong.sei.util.IdGenerator;
import com.changhong.sei.util.thread.ThreadLocalHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


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
    private LogRecordService executionRecordService;
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
        // 属性值hash
        Long attributeCode = baseAttribute.getAttributeCode();

        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCode));
        Pool pool = dao.findOneByFilters(search);
        if (Objects.isNull(pool)) {
            DimensionAttribute attribute = dimensionAttributeService.getAttribute(subjectId, attributeCode);
            if (Objects.isNull(attribute)) {
                ResultData<DimensionAttribute> resultData = dimensionAttributeService.createAttribute(subjectId, baseAttribute);
                if (resultData.failed()) {
                    return ResultData.fail(resultData.getMessage());
                }
                attribute = resultData.getData();
            }

            String periodId = attribute.getPeriod();
            if (StringUtils.isBlank(periodId)) {
                // 创建预算池时,期间不能为空!
                return ResultData.fail(ContextUtil.getMessage("pool_00006"));
            }

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
            // 所属年度
            pool.setYear(period.getYear());
            // 有效期
            pool.setStartDate(period.getStartDate());
            pool.setEndDate(period.getEndDate());
            Category category = categoryService.findOne(order.getCategoryId());
            if (Objects.isNull(category)) {
                // 预算类型不存在
                return ResultData.fail(ContextUtil.getMessage("category_00004", order.getCategoryId()));
            }
            pool.setUse(category.getUse());
            pool.setRoll(category.getRoll());
            pool.setCreatedDate(LocalDateTime.now());

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
     * @param poolCode 预算池编码
     */
    public BigDecimal getPoolBalanceByCode(String poolCode) {
        return poolAmountService.getPoolBalanceByPoolCode(poolCode);
    }

    /**
     * 获取预算池当前可用余额
     *
     * @param pool 预算池
     */
    public BigDecimal getPoolBalance(Pool pool) {
        if (Objects.isNull(pool)) {
            // 未找到预算池
            throw new ServiceException(ContextUtil.getMessage("pool_00001"));
        }
        // 实时计算当前预算池可用金额
        BigDecimal amount = poolAmountService.getPoolBalanceByPoolCode(pool.getCode());
        pool.setBalance(amount);
        return amount;
    }

    /**
     * 记录影响预算池余额的日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void poolAmountLog(String subjectId, long attributeCode, String poolCode,
                              String bizId, String bizCode, String remark, BigDecimal amount,
                              String eventCode, boolean internal, OperationType operation) {
        LogRecord record = new LogRecord(poolCode, internal, operation, amount, eventCode);
        record.setIsPoolAmount(Boolean.TRUE);
        record.setSubjectId(subjectId);
        record.setAttributeCode(attributeCode);
        record.setBizCode(bizCode);
        record.setBizId(bizId);
        record.setBizRemark(remark);
        this.recordLog(record);
    }

    /**
     * 记录不影响预算池余额的日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void nonPoolAmountLog(String subjectId, long attributeCode, String poolCode,
                                 String bizId, String bizCode, String remark, BigDecimal amount,
                                 String eventCode, boolean internal, OperationType operation) {
        LogRecord record = new LogRecord(poolCode, internal, operation, amount, eventCode);
        record.setIsPoolAmount(Boolean.FALSE);
        record.setSubjectId(subjectId);
        record.setAttributeCode(attributeCode);
        record.setBizCode(bizCode);
        record.setBizId(bizId);
        record.setBizRemark(remark);
        this.recordLog(record);
    }

    /**
     * 预算执行日志分为两类:一种是影响预算池余额的日志,另一种是仅记录日志而不影响预算池余额
     * 如:在预占用时,金额大于0,则不影响预算池可用余额;仅当小于0时,才影响预算池可用余额
     *
     * @param record 执行记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLog(LogRecord record) {
        // 操作时间
        record.setOpTime(LocalDateTime.now());
        record.setTimestamp(System.currentTimeMillis());
        // 操作人
        if (StringUtils.isBlank(record.getOpUserAccount())) {
            SessionUser user = ContextUtil.getSessionUser();
            record.setOpUserAccount(user.getAccount());
            record.setOpUserName(user.getUserName());
        }
        // 记录执行日志
        executionRecordService.save(record);

        // 检查当前记录是否影响预算池余额
        if (record.getIsPoolAmount() && StringUtils.isNotBlank(record.getPoolCode())) {
            /*
             在注入或分解是可能还没有预算池,此时只记录日志.
             注入或分解为负数的,必须存在预算池,提交流程时做预占用处理
             */
            Pool pool = dao.findFirstByProperty(Pool.CODE_FIELD, record.getPoolCode());
            if (Objects.nonNull(pool)) {
                // 累计金额
                poolAmountService.countAmount(pool, record.getInternal(), record.getOperation(), record.getAmount());
                // 实时计算当前预算池可用金额
                PoolAmountQuotaDto quota = poolAmountService.getPoolAmountQuota(pool.getCode());
                // 更新预算池金额
                dao.updateAmount(pool.getId(), quota.getTotalAmount(), quota.getUseAmount(), quota.getBalance());
                return;
            }
            LOG.error("预算池[{}]不存在", record.getPoolCode());
        }
    }

    /**
     * 通过Id更新预算池激活状态(冻结/解冻)
     *
     * @param ids      预算池Id集合
     * @param isActive 激活状态
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> updateActiveStatus(Set<String> ids, boolean isActive) {
        List<Pool> pools = dao.findAllById(ids);
        if (CollectionUtils.isNotEmpty(pools)) {
            String bizId = IdGenerator.uuid2();
            String bizCode = DateUtils.formatDate(new Date(), DateUtils.FULL_SEQ_FORMAT);
            for (Pool pool : pools) {
                dao.updateActiveStatus(pool.getId(), isActive);
                if (isActive) {
                    // 解冻预算池
                    this.nonPoolAmountLog(pool.getSubjectId(), pool.getAttributeCode(), pool.getCode(),
                            bizId, bizCode, ContextUtil.getMessage("pool_00023"),
                            pool.getBalance(), Constants.EVENT_BUDGET_UNFREEZE, Boolean.TRUE, OperationType.FREED);
                } else {
                    // 冻结预算池
                    this.nonPoolAmountLog(pool.getSubjectId(), pool.getAttributeCode(), pool.getCode(),
                            bizId, bizCode, ContextUtil.getMessage("pool_00022"),
                            pool.getBalance(), Constants.EVENT_BUDGET_FREEZE, Boolean.TRUE, OperationType.USE);
                }
            }
        }
        return ResultData.success();
    }

    /**
     * 滚动预算池
     *
     * @return 滚动结果
     */
    public ResultData<String> trundlePool() {
        String message = "本次滚动结转预算池: 共%d个, 成功%d个, 失败%d个";
        int sum = 0;
        int success = 0;
        int fail = 0;
        LocalDate localDate = LocalDate.now();
        PeriodType[] periodTypes = new PeriodType[]{PeriodType.SEMIANNUAL, PeriodType.QUARTER, PeriodType.MONTHLY};
        List<Pool> poolList = dao.findExpirePools(localDate.getYear(), localDate, periodTypes);
        if (CollectionUtils.isNotEmpty(poolList)) {
            sum = poolList.size();
            ResultData<Void> resultData;
            String bizId = IdGenerator.uuid2();
            String bizCode = DateUtils.formatDate(new Date(), DateUtils.FULL_SEQ_FORMAT);
            // 为了启用事务,特以此获取bean再调用
            PoolService service = ContextUtil.getBean(PoolService.class);
            // 模拟用户
            MockUser mockUser = new LocalMockUser();
            for (Pool pool : poolList) {
                try {
                    ThreadLocalHolder.begin();
                    SessionUser sessionUser = new SessionUser();
                    sessionUser.setTenantCode(pool.getTenantCode());
                    sessionUser.setUserId("sys");
                    sessionUser.setAccount("sys");
                    sessionUser.setUserName("sys");
                    mockUser.mock(sessionUser);

                    resultData = service.trundlePool(bizId, bizCode, pool.getId());
                    if (LOG.isInfoEnabled()) {
                        LOG.info("{} 预算滚动结转结果: {}", pool.getCode(), resultData);
                    }
                } catch (Exception e) {
                    resultData = ResultData.fail(pool.getCode() + "预算滚动结转异常" + ExceptionUtils.getRootCauseMessage(e));
                    LOG.error(pool.getCode() + " 预算滚动结转异常", e);
                } finally {
                    ThreadLocalHolder.end();
                }
                if (resultData.successful()) {
                    success++;
                } else {
                    fail++;
                }
            }
        }
        return ResultData.success(String.format(message, sum, success, fail));
    }

    /**
     * 滚动预算池
     *
     * @param poolId 预算池id
     * @return 滚动结果
     */
    @SeiLock(key = "'bemsv6:trundle:pool:' + #poolId", fallback = "trundlePoolFallback")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> trundlePool(String bizId, String bizCode, String poolId) {
        Pool pool = dao.findOne(poolId);
        if (Objects.isNull(pool)) {
            // 未找到预算池
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
        if (!pool.getRoll()) {
            // 预算池不允许滚动结转
            return ResultData.fail(ContextUtil.getMessage("pool_00015", pool.getCode()));
        }
        if (BigDecimal.ZERO.compareTo(pool.getBalance()) == 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("预算池[{}]可用余额为0,无需结转.", pool.getCode());
            }
            return ResultData.success();
        }

        // 获取下一预算池
        ResultData<PoolAttributeView> resultData = this.getNextPeriodBudgetPool(pool.getId(), false);
        if (resultData.failed()) {
            return ResultData.fail(resultData.getMessage());
        } else {
            PoolAttributeView nextPool = resultData.getData();
            // 获取当前预算池余额
            BigDecimal balance = this.getPoolBalanceByCode(pool.getCode());
            // 当前预算池
            this.poolAmountLog(pool.getSubjectId(), pool.getAttributeCode(), pool.getCode(),
                    bizId, bizCode, ContextUtil.getMessage("pool_00020", nextPool.getCode()),
                    balance, Constants.EVENT_BUDGET_TRUNDLE, Boolean.TRUE, OperationType.USE);
            // 目标预算池
            this.poolAmountLog(nextPool.getSubjectId(), nextPool.getAttributeCode(), nextPool.getCode(),
                    bizId, bizCode, ContextUtil.getMessage("pool_00021", pool.getCode()),
                    balance, Constants.EVENT_BUDGET_TRUNDLE, Boolean.TRUE, OperationType.RELEASE);
        }
        return ResultData.success();
    }

    /**
     * trundlePool方法的降级处理
     */
    public ResultData<Void> trundlePoolFallback(String bizId, String bizCode, String poolId) {
        return ResultData.fail("预算池[" + poolId + "]正在结转处理中.");
    }

    /**
     * 分页查询预算执行日志
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    public PageResult<LogRecordView> findRecordByPage(Search search) {
        return executionRecordService.findViewByPage(search);
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
     * 初步查找满足条件的预算池
     *
     * @param useBudget 占用数据
     * @return 返回满足条件的预算池
     */
    public List<PoolAttributeView> getBudgetPools(String attribute, LocalDate useDate, BudgetUse useBudget, Collection<SearchFilter> dimFilters) {
        // 公司代码
        String corpCode = useBudget.getCorpCode();
        // 预算科目
        String item = useBudget.getItem();

        Search search = Search.createSearch();
        // 公司代码
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_CORP_CODE, corpCode));
        // 预算维度组合
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ATTRIBUTE, attribute));
        // 预算科目
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ITEM, item));
        // 其他维度条件
        if (CollectionUtils.isNotEmpty(dimFilters)) {
            for (SearchFilter filter : dimFilters) {
                search.addFilter(filter);
            }
        }

        //有效期
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_START_DATE, useDate, SearchFilter.Operator.LE));
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_END_DATE, useDate, SearchFilter.Operator.GE));
        // 启用
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ACTIVE, Boolean.TRUE));
        // 允许使用(业务可用)
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_USE, Boolean.TRUE));

        // 按条件查询满足的预算池
        return poolAttributeDao.findByFilters(search);
    }

    /**
     * 获取同期间预算池(含自己但不含占用日期之前的预算池)
     * 同级期间预算池: 如以1月预算池为基础,获取同维度的2,3,4...12月预算池
     *
     * @param pool 当前预算池
     * @return 返回同期间预算池
     */
    public List<PoolAttributeView> getSamePeriodBudgetPool(PoolAttributeView pool, LocalDate useDate) {
        Search search = Search.createSearch();
        // 预算主体
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_SUBJECT_ID, pool.getSubjectId()));
        // 公司代码
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_CORP_CODE, pool.getCorpCode()));
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_PERIOD_TYPE, pool.getPeriodType()));

        // 按维度属性组装预算池查询条件
        this.doDimensionSearch(search, pool);

        // 占用日期之后的(含自己但不含占用日期之前的预算池)
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_END_DATE, useDate, SearchFilter.Operator.GE));
        // 允许使用(业务可用)
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_USE, Boolean.TRUE));
        // 按起始时间排序
        search.addSortOrder(SearchOrder.asc(PoolAttributeView.FIELD_START_DATE));
        // 按条件查询满足的预算池
        return poolAttributeDao.findByFilters(search);
    }

    /**
     * 获取下一期间的预算池
     * 滚动结转使用
     *
     * @param poolId 预算池id
     * @return 下一期间预算池
     */
    public ResultData<PoolAttributeView> getNextPeriodBudgetPool(String poolId, boolean isAcrossYear) {
        PoolAttributeView poolAttribute = poolAttributeDao.findOne(poolId);
        if (Objects.isNull(poolAttribute)) {
            LOG.error("获取下一期间的预算池错误: 未找到预算池[{}]", poolId);
            return ResultData.fail(ContextUtil.getMessage("pool_00017"));
        }
        // 获取下一期间
        Period nextPeriod;
        ResultData<Period> resultData = periodService.getNextPeriod(poolAttribute.getPeriod(), isAcrossYear);
        if (resultData.failed()) {
            LOG.error("[{}]获取下一期间的预算池错误: {}", poolAttribute.getCode(), resultData.getMessage());
            return ResultData.fail(ContextUtil.getMessage("pool_00018", poolAttribute.getCode(), resultData.getMessage()));
        } else {
            nextPeriod = resultData.getData();
        }

        Search search = Search.createSearch();
        // 预算主体id
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_SUBJECT_ID, poolAttribute.getSubjectId()));
        // 预算期间类型
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_PERIOD_TYPE, poolAttribute.getPeriodType()));
        // 预算期间
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_PERIOD, nextPeriod.getId()));

        // 按维度属性组装预算池查询条件
        this.doDimensionSearch(search, poolAttribute);
        // 按条件查询满足的预算池
        PoolAttributeView poolAttributeView = poolAttributeDao.findFirstByFilters(search);
        if (Objects.isNull(poolAttributeView)) {
            // 未找到滚动结转下一期间的预算池
            return ResultData.fail(ContextUtil.getMessage("pool_00019"));
        } else {
            return ResultData.success(poolAttributeView);
        }
    }

    /**
     * 按预算主体和代码查询预算池
     * 按期间分解使用
     *
     * @param subjectId     预算主体id
     * @param baseAttribute 预算维度属性
     * @return 预算池
     */
    public PoolAttributeView getParentPeriodBudgetPool(String subjectId, BaseAttribute baseAttribute) {
        String periodId = baseAttribute.getPeriod();
        Period period = periodService.findOne(periodId);
        if (Objects.isNull(period)) {
            // 预算期间不存在
            LOG.error(ContextUtil.getMessage("period_00002"));
            return null;
        }

        PeriodType periodType = period.getType();
        LocalDate sDate = period.getStartDate();
        LocalDate eDate = period.getEndDate();

        PoolAttributeView pool = null;
        switch (periodType) {
            case CUSTOMIZE:
            case ANNUAL:
                LOG.error("年度或自定义期间,不支持分解.");
                break;
            case MONTHLY:
                pool = this.getParentPeriodBudgetPool(subjectId, baseAttribute, PeriodType.QUARTER, sDate, eDate);
                if (Objects.nonNull(pool)) {
                    break;
                }
            case QUARTER:
                pool = this.getParentPeriodBudgetPool(subjectId, baseAttribute, PeriodType.SEMIANNUAL, sDate, eDate);
                if (Objects.nonNull(pool)) {
                    break;
                }
            case SEMIANNUAL:
                pool = this.getParentPeriodBudgetPool(subjectId, baseAttribute, PeriodType.ANNUAL, sDate, eDate);
                if (Objects.nonNull(pool)) {
                    break;
                }
            default:
                pool = null;
        }
        return pool;
    }

    /**
     * 按预算主体和代码查询预算池
     *
     * @param subjectId     预算主体id
     * @param baseAttribute 预算维度属性
     * @return 预算池
     */
    private PoolAttributeView getParentPeriodBudgetPool(String subjectId, BaseAttribute baseAttribute, PeriodType periodType, LocalDate sDate, LocalDate eDate) {
        Search search = Search.createSearch();
        // 预算主体id
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_SUBJECT_ID, subjectId));
        // 预算期间类型
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_PERIOD_TYPE, periodType));

        // 按维度属性组装预算池查询条件
        this.doDimensionSearch(search, baseAttribute);

        // 周期范围内
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_START_DATE, sDate, SearchFilter.Operator.LE));
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_END_DATE, eDate, SearchFilter.Operator.GE));
        // 按起始时间排序
        search.addSortOrder(SearchOrder.asc(PoolAttributeView.FIELD_START_DATE));
        // 按条件查询满足的预算池
        return poolAttributeDao.findFirstByFilters(search);
    }

    /**
     * 按维度属性组装预算池查询条件
     *
     * @param search        查询对象
     * @param baseAttribute 维度属性
     */
    private void doDimensionSearch(Search search, BaseAttribute baseAttribute) {
        // 预算维度组合
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ATTRIBUTE, baseAttribute.getAttribute()));
        // 预算科目
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ITEM, baseAttribute.getItem()));
        // 组织
        if (StringUtils.isNotBlank(baseAttribute.getOrg())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ORG, baseAttribute.getOrg()));
        }
        // 项目
        if (StringUtils.isNotBlank(baseAttribute.getProject())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_PROJECT, baseAttribute.getProject()));
        }
        // 自定义1
        if (StringUtils.isNotBlank(baseAttribute.getUdf1())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_UDF1, baseAttribute.getUdf1()));
        }
        // 自定义2
        if (StringUtils.isNotBlank(baseAttribute.getUdf2())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_UDF2, baseAttribute.getUdf2()));
        }
        // 自定义3
        if (StringUtils.isNotBlank(baseAttribute.getUdf3())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_UDF3, baseAttribute.getUdf3()));
        }
        // 自定义4
        if (StringUtils.isNotBlank(baseAttribute.getUdf4())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_UDF4, baseAttribute.getUdf4()));
        }
        // 自定义5
        if (StringUtils.isNotBlank(baseAttribute.getUdf5())) {
            search.addFilter(new SearchFilter(PoolAttributeView.FIELD_UDF5, baseAttribute.getUdf5()));
        }
        // 启用
        search.addFilter(new SearchFilter(PoolAttributeView.FIELD_ACTIVE, Boolean.TRUE));
    }
}