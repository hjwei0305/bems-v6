package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.util.DateUtils;
import com.changhong.sei.util.IdGenerator;
import com.changhong.sei.util.thread.ThreadLocalHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预算池(Pool)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Service
public class PoolService {
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
    private StrategyItemService strategyItemService;
    @Autowired
    private PoolAmountService poolAmountService;
    @Autowired
    private PoolLogService poolLogService;
    @Autowired
    private StrategyPeriodService strategyPeriodService;
    @Autowired
    private MockUser mockUser;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 按预算池编码获取预算池
     *
     * @param poolCode 预算池编码
     * @return 返回满足条件的预算池
     */
    public Pool getPool(String poolCode) {
        return dao.findFirstByProperty(Pool.FIELD_CODE, poolCode);
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
     * @param subjectId      预算主体id
     * @param categoryId     预算类型id
     * @param currencyCode   币种
     * @param managerOrgCode 归口管理部门
     * @param periodType     预算期间类型
     * @param baseAttribute  预算维度属性
     * @param injectAmount   注入金额.通过注入且新产生预算池时的金额,作为初始注入金额,用于多维分析的差异计算
     * @param reviseInAmount 调入金额.新产生预算池时的金额,作为初始注入金额,用于预算池分析的差异计算
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Pool> createPool(String subjectId, String categoryId, String currencyCode, String currencyName,
                                       String managerOrgCode, String managerOrgName, PeriodType periodType,
                                       BaseAttribute baseAttribute, BigDecimal injectAmount, BigDecimal reviseInAmount) {
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
            pool = new Pool();
            // 预算主体
            pool.setSubjectId(subjectId);
            // 属性id
            pool.setAttributeCode(attributeCode);
            // 币种
            pool.setCurrencyCode(currencyCode);
            pool.setCurrencyName(currencyName);
            // 归口管理部门
            pool.setManageOrg(managerOrgCode);
            pool.setManageOrgName(managerOrgName);
            // 期间类型
            pool.setPeriodType(periodType);

            Category category = categoryService.findOne(categoryId);
            if (Objects.isNull(category)) {
                // 预算类型不存在
                return ResultData.fail(ContextUtil.getMessage("category_00004", categoryId));
            }

            return this.createPool(pool, baseAttribute, injectAmount, reviseInAmount);
        }
        return ResultData.success(pool);
    }

    /**
     * 创建预算池
     *
     * @param pool           预算池
     * @param baseAttribute  维度属性
     * @param injectAmount   注入金额.通过注入且新产生预算池时的金额,作为初始注入金额,用于多维分析的差异计算
     * @param reviseInAmount 调入金额.新产生预算池时的金额,作为初始注入金额,用于预算池分析的差异计算
     * @return 创建结果
     */
    private ResultData<Pool> createPool(Pool pool, BaseAttribute baseAttribute, BigDecimal injectAmount, BigDecimal reviseInAmount) {
        String subjectId = pool.getSubjectId();
        // 属性值hash
        Long attributeCode = baseAttribute.getAttributeCode();
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
        // 检查预算期间
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

        pool.setCreatedDate(LocalDateTime.now());

        // 租户代码
        String tenantCode = ContextUtil.getTenantCode();
        // 预算池编码
        // String code = serialService.getNumber(Pool.class, tenantCode);
        String now = DateUtils.formatDate(new Date(), "yyMMdd");
        String key = Constants.POOL_CODE_CACHE_KEY_PREFIX.concat(now);
        String code;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
        }
        code = now + String.format("%06d", redisTemplate.opsForValue().increment(key, 1));
        // if (dao.isCodeExists(tenantCode, code, IdGenerator.uuid())) {
        //     code = now + String.format("%06d", redisTemplate.opsForValue().increment(key, 1));
        // }
        pool.setTenantCode(tenantCode);
        pool.setCode(code);
        dao.save(pool);
        // 创建预算池时初始化预算池维度属性金额
        poolAmountService.initAmount(pool, injectAmount, reviseInAmount);

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
     * 记录影响预算池余额的日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void poolAmountLog(Pool pool, String bizId, String bizCode, String remark, BigDecimal amount,
                              String eventCode, boolean internal, OperationType operation) {
        PoolLog record = new PoolLog(pool.getCode(), internal, operation, amount, eventCode);
        record.setIsPoolAmount(Boolean.TRUE);
        record.setSubjectId(pool.getSubjectId());
        record.setAttributeCode(pool.getAttributeCode());
        record.setBizCode(bizCode);
        record.setBizId(bizId);
        record.setBizRemark(remark);
        this.recordLog(record);
    }

    /**
     * 记录不影响预算池余额的日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void nonPoolAmountLog(Pool pool, String bizId, String bizCode, String remark, BigDecimal amount,
                                 String eventCode, boolean internal, OperationType operation) {
        PoolLog record = new PoolLog(pool.getCode(), internal, operation, amount, eventCode);
        record.setIsPoolAmount(Boolean.FALSE);
        record.setSubjectId(pool.getSubjectId());
        record.setAttributeCode(pool.getAttributeCode());
        record.setBizCode(bizCode);
        record.setBizId(bizId);
        record.setBizRemark(remark);
        this.recordLog(record);
    }

    /**
     * 预算执行日志分为两类:一种是影响预算池余额的日志,另一种是仅记录日志而不影响预算池余额
     * 如:在预占用时,金额大于0,则不影响预算池可用余额;仅当小于0时,才影响预算池可用余额
     *
     * @param poolLog 执行记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLog(PoolLog poolLog) {
        // 记录执行日志
        poolLogService.addLogRecord(poolLog);

        // 检查当前记录是否影响预算池余额
        if (poolLog.getIsPoolAmount()) {
            /*
             在注入或分解是可能还没有预算池,此时只记录日志.
             注入或分解为负数的,必须存在预算池,提交流程时做预占用处理
             */
            Pool pool = dao.findFirstByProperty(Pool.CODE_FIELD, poolLog.getPoolCode());
            if (Objects.nonNull(pool)) {
                // 累计金额
                poolAmountService.countAmount(pool, poolLog.getInternal(), poolLog.getOperation(), poolLog.getAmount());
                // 实时计算当前预算池可用金额
                PoolAmountQuotaDto quota = poolAmountService.getPoolAmountQuota(pool.getCode());
                // 更新预算池金额
                dao.updateAmount(pool.getId(), quota.getTotalAmount(), quota.getUseAmount(), quota.getBalance());
            } else {
                LOG.error("预算池[{}]不存在", poolLog.getPoolCode());
            }
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
                    this.nonPoolAmountLog(pool, bizId, bizCode, ContextUtil.getMessage("pool_00023"),
                            pool.getBalance(), Constants.EVENT_BUDGET_UNFREEZE, Boolean.TRUE, OperationType.FREED);
                } else {
                    // 冻结预算池
                    this.nonPoolAmountLog(pool, bizId, bizCode, ContextUtil.getMessage("pool_00022"),
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
        String key = "bemsv6:trundle:pool:batch";
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                // 正在结转处理中
                return ResultData.fail(ContextUtil.getMessage("pool_00036"));
            }
            redisTemplate.opsForValue().set(key, "true", 10, TimeUnit.MINUTES);
            int sum = 0;
            LocalDate localDate = LocalDate.now();
            // 获取超过指定日期的非自定义期间类型预算池
            List<Pool> poolList = dao.findNoCustomizeExpirePools(localDate.getYear(), localDate);
            if (CollectionUtils.isNotEmpty(poolList)) {
                sum = poolList.size();
                String bizId = IdGenerator.uuid2();
                String bizCode = DateUtils.formatDate(new Date(), DateUtils.FULL_SEQ_FORMAT);
                SessionUser sessionUser = ContextUtil.getSessionUser();
                // 为了启用事务,特以此获取bean再调用
                PoolService service = ContextUtil.getBean(PoolService.class);
                poolList.parallelStream().forEach(pool -> {
                    ThreadLocalHolder.begin();
                    try {
                        mockUser.mock(sessionUser);

                        ResultData<Void> resultData = service.trundlePool(pool, bizId, bizCode);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("{} 预算滚动结转结果: {}", pool.getCode(), resultData);
                        }
                    } catch (Exception e) {
                        LOG.error(pool.getCode() + " 预算滚动结转异常", e);
                    } finally {
                        ThreadLocalHolder.end();
                    }
                });
            }
            // 本次共有[{0}]个预算需要做滚动结转.
            return ResultData.success(ContextUtil.getMessage("pool_00035", sum));
        } finally {
            redisTemplate.delete(key);
        }
    }

    /**
     * 滚动预算池
     *
     * @param poolId 预算池id
     * @return 滚动结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> trundlePool(String bizId, String bizCode, String poolId) {
        Pool pool = dao.findOne(poolId);
        // 为了启用事务,特以此获取bean再调用
        return this.trundlePool(pool, bizId, bizCode);
    }

    /**
     * 滚动预算池
     *
     * @param pool 预算池
     * @return 滚动结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> trundlePool(Pool pool, String bizId, String bizCode) {
        if (Objects.isNull(pool)) {
            // 未找到预算池
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
        if (BigDecimal.ZERO.compareTo(pool.getBalance()) == 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("预算池[{}]可用余额为0,无需结转.", pool.getCode());
            }
            return ResultData.success();
        }
        String key = "bemsv6:trundle:pool:".concat(pool.getCode());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return ResultData.fail(ContextUtil.getMessage("pool_00037", pool.getCode()));
        }
        redisTemplate.opsForValue().set(key, "true", 10, TimeUnit.MINUTES);
        try {
            // 检查预算期间类型控制策略,是否可结转
            StrategyPeriod subjectPeriod = strategyPeriodService.getSubjectPeriod(pool.getSubjectId(), pool.getPeriodType());
            if (Objects.isNull(subjectPeriod)) {
                // 预算池[{0}]对应的预算主体未配置期间类型[{1}]的控制策略!
                return ResultData.fail(ContextUtil.getMessage("pool_00019", pool.getCode(), pool.getPeriodType()));
            }
            if (Boolean.FALSE.equals(subjectPeriod.getRoll())) {
                // 预算池不允许滚动结转
                return ResultData.fail(ContextUtil.getMessage("pool_00015", pool.getCode()));
            }
            // 是否跨年
            boolean isAcrossYear = false;
            if (12 == pool.getEndDate().getMonthValue()) {
                // 检查年度期间配置是否允许跨年
                subjectPeriod = strategyPeriodService.getSubjectPeriod(pool.getSubjectId(), PeriodType.ANNUAL);
                if (Objects.nonNull(subjectPeriod) && subjectPeriod.getRoll()) {
                    isAcrossYear = true;
                }
            }

            // 获取当前预算池余额
            BigDecimal balance = this.getPoolBalanceByCode(pool.getCode());
            // 获取下一预算池
            ResultData<Pool> resultData = this.getOrCreateNextPeriodBudgetPool(pool.getId(), balance, isAcrossYear);
            if (resultData.failed()) {
                return ResultData.fail(resultData.getMessage());
            } else {
                Pool nextPool = resultData.getData();
                // 当前预算池
                this.poolAmountLog(pool, bizId, bizCode, ContextUtil.getMessage("pool_00020", nextPool.getCode()),
                        balance, Constants.EVENT_BUDGET_TRUNDLE, Boolean.TRUE, OperationType.USE);
                // 目标预算池
                this.poolAmountLog(nextPool, bizId, bizCode, ContextUtil.getMessage("pool_00021", pool.getCode()),
                        balance, Constants.EVENT_BUDGET_TRUNDLE, Boolean.TRUE, OperationType.RELEASE);
            }
            return ResultData.success();
        } catch (Exception e) {
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultData.fail(e.getMessage());
        } finally {
            redisTemplate.delete(key);
        }
    }

    /**
     * 分页查询预算执行日志
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    public PageResult<PoolLogDto> findRecordByPage(Search search) {
        return poolLogService.findByPage(search);
    }

    /**
     * 分页查询预算池
     *
     * @param param 查询对象
     * @return 分页结果
     */
    public PageResult<PoolAttributeDto> findPoolByPage(PoolQuickQueryParam param) {
        PageResult<PoolAttributeDto> pageResult = dao.queryPoolPaging(param);
        if (pageResult.getTotal() > 0) {
            StrategyDto strategy;
            StrategyPeriod strategyPeriod;
            ResultData<StrategyDto> resultData;
            List<PoolAttributeDto> list = pageResult.getRows();
            Map<String, StrategyPeriod> strategyPeriodMap = new HashMap<>();
            Map<String, StrategyDto> strategyMap = new HashMap<>();
            for (PoolAttributeDto pool : list) {
                String subjectId = pool.getSubjectId();
                strategyPeriod = strategyPeriodMap.get(subjectId + pool.getPeriodType().name());
                if (Objects.isNull(strategyPeriod)) {
                    strategyPeriod = strategyPeriodService.getSubjectPeriod(subjectId, pool.getPeriodType());
                    strategyPeriodMap.put(subjectId + pool.getPeriodType().name(), strategyPeriod);
                }
                if (Objects.nonNull(strategyPeriod)) {
                    pool.setRoll(strategyPeriod.getRoll());
                    pool.setUse(strategyPeriod.getUse());
                }
                strategy = strategyMap.get(subjectId + pool.getItem());
                if (Objects.isNull(strategy)) {
                    resultData = strategyItemService.getStrategy(subjectId, pool.getItem());
                    if (resultData.successful()) {
                        strategy = resultData.getData();
                        strategyMap.put(subjectId + pool.getItem(), strategy);
                    }
                }
                if (Objects.nonNull(strategy)) {
                    pool.setStrategyId(strategy.getCode());
                    pool.setStrategyName(strategy.getName());
                }
            }
            strategyMap.clear();
        }
        return pageResult;
    }

    /**
     * 按预算池id获取预算池
     *
     * @param id 预算池id
     * @return 预算池
     */
    public ResultData<PoolAttributeDto> findPoolAttribute(String id) {
        Pool pool = dao.findOne(id);
        return this.getPoolAttribute(pool);
    }

    /**
     * 按预算池代码获取预算池
     *
     * @param codes 预算池代码清单
     * @return 预算池
     */
    public List<PoolAttributeDto> findPoolAttributes(List<String> codes) {
        List<PoolAttributeDto> list = new ArrayList<>();
        List<Pool> poolList = dao.findByFilter(new SearchFilter(Pool.CODE_FIELD, codes, SearchFilter.Operator.IN));
        for (Pool pool : poolList) {
            PoolAttributeDto dto = this.constructPoolAttribute(pool, Optional.empty());

            DimensionAttribute attribute = dimensionAttributeService.getAttribute(pool.getSubjectId(), pool.getAttributeCode());
            if (Objects.nonNull(attribute)) {
                this.putAttribute(dto, attribute);
            } else {
                LOG.error("预算池[{}]未获取到维度属性", pool.getCode());
            }

            ResultData<StrategyDto> resultData = strategyItemService.getStrategy(dto.getSubjectId(), dto.getItem());
            if (resultData.successful()) {
                StrategyDto strategy = resultData.getData();
                // 策略
                dto.setStrategyId(strategy.getCode());
                dto.setStrategyName(strategy.getName());
            } else {
                LOG.error("预算池[{}]获取执行策略错误: {}", pool.getCode(), resultData.getMessage());
            }
            list.add(dto);
        }
        return list;
    }

    /**
     * 按预算池id获取预算池
     *
     * @param code 预算池代码
     * @return 预算池
     */
    public ResultData<PoolAttributeDto> findPoolAttributeByCode(String code) {
        Pool pool = dao.findFirstByProperty(Pool.FIELD_CODE, code);
        return this.getPoolAttribute(pool);
    }

    /**
     * 初步查找满足条件的预算池
     *
     * @param subjectId 预算主体
     * @param item      预算科目
     * @return 返回满足条件的预算池
     */
    public List<PoolAttributeDto> getBudgetPools(String subjectId, String attribute, LocalDate useDate, String item,
                                                 Collection<SearchFilter> dimFilters) {
        List<SearchFilter> filters;
        {
            // 在其他维度条件基础上追加预算科目
            if (Objects.isNull(dimFilters)) {
                filters = new ArrayList<>();
            } else {
                filters = new ArrayList<>(dimFilters);
            }
            filters.add(new SearchFilter(DimensionAttribute.FIELD_ITEM, item));
        }
        // 按预算主体和维度查询满足要求的预算维度属性
        List<DimensionAttribute> attributeList = dimensionAttributeService.getAttributes(subjectId, attribute, filters);
        return this.getPoolAttributes(subjectId, useDate, attributeList, Optional.empty());
    }

    /**
     * 获取同期间预算池(含自己但不含占用日期之前的预算池)
     * 同级期间预算池: 如以1月预算池为基础,获取同维度的2,3,4...12月预算池
     *
     * @param poolAttribute 当前预算池
     * @return 返回同期间预算池
     */
    public List<PoolAttributeDto> getSamePeriodBudgetPool(PoolAttributeDto poolAttribute, LocalDate useDate) {
        DimensionAttribute attribute = new DimensionAttribute();
        attribute.setAttribute(poolAttribute.getAttribute());
        attribute.setItem(poolAttribute.getItem());
        attribute.setPeriod(poolAttribute.getPeriod());
        attribute.setOrg(poolAttribute.getOrg());
        attribute.setProject(poolAttribute.getProject());
        attribute.setUdf1(poolAttribute.getUdf1());
        attribute.setUdf2(poolAttribute.getUdf2());
        attribute.setUdf3(poolAttribute.getUdf3());
        attribute.setUdf4(poolAttribute.getUdf4());
        attribute.setUdf5(poolAttribute.getUdf5());

        // 按预算主体和维度查询满足要求的预算维度属性
        List<DimensionAttribute> attributeList = dimensionAttributeService.getAttributes(poolAttribute.getSubjectId(), attribute);
        return this.getPoolAttributes(poolAttribute.getSubjectId(), useDate, attributeList, Optional.of(poolAttribute.getPeriodType()));
    }

    private List<PoolAttributeDto> getPoolAttributes(String subjectId, LocalDate useDate, List<DimensionAttribute> attributeList, Optional<PeriodType> periodType) {
        List<PoolAttributeDto> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(attributeList)) {
            return resultList;
        }
        // 维度属性按主体和散列值分组
        Map<String, DimensionAttribute> attributeMap = attributeList.stream().collect(Collectors.toMap(a -> a.getSubjectId() + a.getAttributeCode(), a -> a));
        // 维度属性散列值清单
        List<Long> attributeCodes = attributeList.stream().map(DimensionAttribute::getAttributeCode).collect(Collectors.toList());
        attributeList.clear();

        Search search = Search.createSearch();
        // 预算主体
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        // 预算维度
        if (attributeCodes.size() > 1) {
            search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes, SearchFilter.Operator.IN));
        } else {
            search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes.get(0)));
        }
        if (periodType.isPresent()) {
            // 预算期间类型
            search.addFilter(new SearchFilter(Pool.FIELD_PERIOD_TYPE, periodType.get()));
            // 占用日期之后的(含自己但不含占用日期之前的预算池)
            search.addFilter(new SearchFilter(Pool.FIELD_END_DATE, useDate, SearchFilter.Operator.GE));
        } else {
            //有效期
            search.addFilter(new SearchFilter(Pool.FIELD_START_DATE, useDate, SearchFilter.Operator.LE));
            search.addFilter(new SearchFilter(Pool.FIELD_END_DATE, useDate, SearchFilter.Operator.GE));
        }

        // 启用
        search.addFilter(new SearchFilter(Pool.FIELD_ACTIVED, Boolean.TRUE));
        // 按条件查询满足的预算池
        List<Pool> poolList = dao.findByFilters(search);
        if (CollectionUtils.isNotEmpty(poolList)) {
            Map<PeriodType, StrategyPeriod> periodMap;
            // 按预算主体获取预算期间类型控制策略
            List<StrategyPeriod> subjectPeriods = strategyPeriodService.findBySubject(subjectId);
            if (CollectionUtils.isNotEmpty(subjectPeriods)) {
                periodMap = subjectPeriods.stream().collect(Collectors.toMap(StrategyPeriod::getPeriodType, p -> p));
            } else {
                periodMap = new HashMap<>();
            }
            PoolAttributeDto dto;
            StrategyDto strategy;
            ResultData<StrategyDto> resultData;
            DimensionAttribute dimensionAttribute;
            StrategyPeriod subjectPeriod;
            for (Pool pool : poolList) {
                subjectPeriod = periodMap.get(pool.getPeriodType());
                // 检查期间类型控制策略,是否允许使用
                if (Objects.isNull(subjectPeriod) || Boolean.FALSE.equals(subjectPeriod.getUse())) {
                    continue;
                }
                dto = this.constructPoolAttribute(pool, Optional.of(subjectPeriod));

                dimensionAttribute = attributeMap.get(pool.getSubjectId() + pool.getAttributeCode());
                if (Objects.nonNull(dimensionAttribute)) {
                    resultData = strategyItemService.getStrategy(pool.getSubjectId(), dimensionAttribute.getItem());
                    if (resultData.successful()) {
                        strategy = resultData.getData();
                        dto.setStrategyId(strategy.getCode());
                        dto.setStrategyName(strategy.getName());
                        // 预算维度属性赋值
                        this.putAttribute(dto, dimensionAttribute);
                        resultList.add(dto);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 获取下一期间的预算池
     * 滚动结转使用
     *
     * @param poolId 预算池id
     * @return 下一期间预算池
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Pool> getOrCreateNextPeriodBudgetPool(String poolId, BigDecimal balance, boolean isAcrossYear) {
        Pool pool = dao.findOne(poolId);
        if (Objects.isNull(pool)) {
            LOG.error("获取下一期间的预算池错误: 未找到当前预算池[{}]", poolId);
            return ResultData.fail(ContextUtil.getMessage("pool_00017", poolId));
        }
        // 按预算池的主体和维度属性散列值获取维度属性对象
        DimensionAttribute attribute = dimensionAttributeService.getAttribute(pool.getSubjectId(), pool.getAttributeCode());
        if (Objects.isNull(attribute)) {
            LOG.error("获取下一期间的预算池错误: 未找到预算池[{}]的维度属性", pool.getCode());
            return ResultData.fail(ContextUtil.getMessage("pool_00007", pool.getCode()));
        }
        // 获取下一期间
        Period nextPeriod;
        ResultData<Period> resultData = periodService.getNextPeriod(attribute.getPeriod(), isAcrossYear);
        if (resultData.failed()) {
            LOG.error("[{}]获取下一期间的预算池错误: {}", pool.getCode(), resultData.getMessage());
            return ResultData.fail(ContextUtil.getMessage("pool_00018", pool.getCode(), resultData.getMessage()));
        } else {
            nextPeriod = resultData.getData();
        }

        Pool nextPeriodPool = null;
        // 按预算主体和下级期间查询匹配的预算维度属性
        List<DimensionAttribute> attributeList = dimensionAttributeService.getAttributes(pool.getSubjectId(), nextPeriod.getId(), attribute);
        if (CollectionUtils.isNotEmpty(attributeList)) {
            List<Long> attributeCodes = attributeList.stream().map(DimensionAttribute::getAttributeCode).collect(Collectors.toList());

            Search search = Search.createSearch();
            // 预算主体id
            search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, pool.getSubjectId()));
            // 预算期间类型
            search.addFilter(new SearchFilter(Pool.FIELD_PERIOD_TYPE, pool.getPeriodType()));
            // 预算维度属性散列值
            if (attributeCodes.size() > 1) {
                search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes, SearchFilter.Operator.IN));
            } else {
                search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes.get(0)));
            }
            // 启用
            search.addFilter(new SearchFilter(Pool.FIELD_ACTIVED, Boolean.TRUE));
            // 按条件查询满足的预算池
            nextPeriodPool = dao.findFirstByFilters(search);
        }
        // 下一期间预算池是否存在,不存在则创建
        if (Objects.isNull(nextPeriodPool)) {
            // 设置下一期间
            attribute.setPeriod(nextPeriod.getId());
            attribute.setPeriodName(nextPeriod.getName());

            nextPeriodPool = new Pool();
            // 预算主体
            nextPeriodPool.setSubjectId(pool.getSubjectId());
            // 属性id
            nextPeriodPool.setAttributeCode(attribute.getAttributeCode());
            // 币种
            nextPeriodPool.setCurrencyCode(pool.getCurrencyCode());
            nextPeriodPool.setCurrencyName(pool.getCurrencyName());
            // 归口管理部门
            nextPeriodPool.setManageOrg(pool.getManageOrg());
            nextPeriodPool.setManageOrgName(pool.getManageOrgName());
            // 期间类型
            nextPeriodPool.setPeriodType(pool.getPeriodType());
            // 创建预算池
            return this.createPool(nextPeriodPool, attribute, BigDecimal.ZERO, balance);
        } else {
            return ResultData.success(nextPeriodPool);
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
    public Pool getParentPeriodBudgetPool(String subjectId, BaseAttribute baseAttribute) {
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

        Pool pool = null;
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
    private Pool getParentPeriodBudgetPool(String subjectId, BaseAttribute baseAttribute, PeriodType periodType, LocalDate sDate, LocalDate eDate) {
        List<DimensionAttribute> attributeList = dimensionAttributeService.getAttributes(subjectId, baseAttribute);
        if (CollectionUtils.isEmpty(attributeList)) {
            return null;
        }
        List<Long> attributeCodes = attributeList.stream().map(DimensionAttribute::getAttributeCode).collect(Collectors.toList());

        Search search = Search.createSearch();
        // 预算主体id
        search.addFilter(new SearchFilter(Pool.FIELD_SUBJECT_ID, subjectId));
        // 预算维度属性散列值
        if (attributeCodes.size() > 1) {
            search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes, SearchFilter.Operator.IN));
        } else {
            search.addFilter(new SearchFilter(Pool.FIELD_ATTRIBUTE_CODE, attributeCodes.get(0)));
        }
        // 预算期间类型
        search.addFilter(new SearchFilter(Pool.FIELD_PERIOD_TYPE, periodType));
        // 启用
        search.addFilter(new SearchFilter(Pool.FIELD_ACTIVED, Boolean.TRUE));
        // 周期范围内
        search.addFilter(new SearchFilter(Pool.FIELD_START_DATE, sDate, SearchFilter.Operator.LE));
        search.addFilter(new SearchFilter(Pool.FIELD_END_DATE, eDate, SearchFilter.Operator.GE));
        // 按起始时间排序
        search.addSortOrder(SearchOrder.asc(Pool.FIELD_START_DATE));
        // 按条件查询满足的预算池
        return dao.findFirstByFilters(search);
    }

    /**
     * 获取预算池全量信息
     *
     * @param pool 预算池
     * @return 预算池
     */
    private ResultData<PoolAttributeDto> getPoolAttribute(Pool pool) {
        if (Objects.isNull(pool)) {
            // 未找到预算池
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
        StrategyPeriod subjectPeriod = strategyPeriodService.getSubjectPeriod(pool.getSubjectId(), pool.getPeriodType());
        PoolAttributeDto dto = this.constructPoolAttribute(pool, Optional.ofNullable(subjectPeriod));

        DimensionAttribute attribute = dimensionAttributeService.getAttribute(pool.getSubjectId(), pool.getAttributeCode());
        if (Objects.nonNull(attribute)) {
            this.putAttribute(dto, attribute);
        } else {
            // 预算池[{0}]维度属性错误!
            return ResultData.fail(ContextUtil.getMessage("pool_00007", pool.getCode()));
        }

        ResultData<StrategyDto> resultData = strategyItemService.getStrategy(dto.getSubjectId(), dto.getItem());
        if (resultData.successful()) {
            StrategyDto strategy = resultData.getData();
            // 策略
            dto.setStrategyId(strategy.getCode());
            dto.setStrategyName(strategy.getName());
        } else {
            return ResultData.fail(resultData.getMessage());
        }
        return ResultData.success(dto);
    }

    private PoolAttributeDto constructPoolAttribute(Pool pool, Optional<StrategyPeriod> periodOptional) {
        PoolAttributeDto dto = new PoolAttributeDto();
        dto.setId(pool.getId());
        dto.setCode(pool.getCode());
        dto.setSubjectId(pool.getSubjectId());
        dto.setCurrencyCode(pool.getCurrencyCode());
        dto.setCurrencyName(pool.getCurrencyName());
        dto.setManageOrg(pool.getManageOrg());
        dto.setManageOrgName(pool.getManageOrgName());
        dto.setPeriodType(pool.getPeriodType());
        dto.setYear(pool.getYear());
        dto.setStartDate(pool.getStartDate());
        dto.setEndDate(pool.getEndDate());
        dto.setActived(pool.getActived());
        // 检查预算期间类型控制策略,是否可结转
        if (periodOptional.isPresent()) {
            StrategyPeriod subjectPeriod = periodOptional.get();
            dto.setUse(subjectPeriod.getUse());
            dto.setRoll(subjectPeriod.getRoll());
        }
        dto.setDelay(pool.getDelay());
        dto.setTotalAmount(pool.getTotalAmount());
        dto.setUsedAmount(pool.getUsedAmount());
        dto.setBalance(pool.getBalance());
        return dto;
    }

    private void putAttribute(BaseAttributeDto dto, DimensionAttribute attribute) {
        dto.setAttribute(attribute.getAttribute());
        dto.setAttributeCode(attribute.getAttributeCode());
        dto.setPeriod(attribute.getPeriod());
        dto.setPeriodName(attribute.getPeriodName());
        dto.setItem(attribute.getItem());
        dto.setItemName(attribute.getItemName());
        dto.setOrg(attribute.getOrg());
        dto.setOrgName(attribute.getOrgName());
        dto.setProject(attribute.getProject());
        dto.setProjectName(attribute.getProjectName());
        dto.setUdf1(attribute.getUdf1());
        dto.setUdf1Name(attribute.getUdf1Name());
        dto.setUdf2(attribute.getUdf2());
        dto.setUdf2Name(attribute.getUdf2Name());
        dto.setUdf3(attribute.getUdf3());
        dto.setUdf3Name(attribute.getUdf3Name());
        dto.setUdf4(attribute.getUdf4());
        dto.setUdf4Name(attribute.getUdf4Name());
        dto.setUdf5(attribute.getUdf5());
        dto.setUdf5Name(attribute.getUdf5Name());
    }
}