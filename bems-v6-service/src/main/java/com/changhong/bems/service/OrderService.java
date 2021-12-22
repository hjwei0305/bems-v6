package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.cust.BudgetDimensionCustManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.edm.sdk.DocumentManager;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.serial.sdk.SerialService;
import com.changhong.sei.util.EnumUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    private static final String NUM_REGEX = "-?[0-9]+.?[0-9]*";
    @Autowired
    private OrderDao dao;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private OrderCommonService orderCommonService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private OrganizationManager organizationManager;
    @Autowired(required = false)
    private SerialService serialService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DocumentManager documentManager;
    @Autowired
    private BudgetDimensionCustManager budgetDimensionCustManager;

    @Override
    protected BaseEntityDao<Order> getDao() {
        return dao;
    }

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    public ResultData<List<OrganizationDto>> findOrgTree() {
        return organizationManager.findOrgTreeWithoutFrozen();
    }

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    public PageResult<OrderDetail> getOrderItems(String orderId, Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, orderId));
        return orderDetailService.findByPage(search);
    }

    /**
     * 导出预算订单明细数据
     *
     * @param orderId 订单id
     * @return 导出的明细数据
     */
    public ResultData<Map<String, Object>> exportBudgeDetails(String orderId) {
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        LinkedList<TemplateHeadVo> head = new LinkedList<>();
        List<DimensionDto> dimensions = categoryService.getAssigned(order.getCategoryId());
        if (CollectionUtils.isNotEmpty(dimensions)) {
            int index = 0;
            for (DimensionDto dto : dimensions) {
                head.add(new TemplateHeadVo(index++, dto.getCode().concat("Name"), ContextUtil.getMessage("default_dimension_" + dto.getCode())));
            }
            head.add(new TemplateHeadVo(index, OrderDetail.FIELD_AMOUNT, ContextUtil.getMessage("budget_template_amount")));

            // 检查是否存在错误行项
            long hasErrCount = orderDetailService.getHasErrCount(orderId);
            if (hasErrCount > 0) {
                head.add(new TemplateHeadVo(index + 1, OrderDetail.FIELD_ERRMSG, ContextUtil.getMessage("budget_template_errmsg")));
            }

            Map<String, Object> data = new HashMap<>(7);
            data.put("head", head);
            List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
            data.put("data", details);
            return ResultData.success(data);
        } else {
            // 预算类型[{0}]下未找到预算维度
            return ResultData.fail(ContextUtil.getMessage("category_00007", order.getCategoryName()));
        }
    }

    /**
     * 获取预算模版格式数据
     *
     * @param categoryId 预算类型id
     * @return 预算模版格式数据
     */
    public LinkedList<TemplateHeadVo> getBudgetTemplate(String categoryId) {
        LinkedList<TemplateHeadVo> list = new LinkedList<>();
        List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
        if (CollectionUtils.isNotEmpty(dimensions)) {
            int index = 0;
            for (DimensionDto dto : dimensions) {
                list.add(new TemplateHeadVo(index++, dto.getCode(), ContextUtil.getMessage("default_dimension_" + dto.getCode())));
            }
            list.add(new TemplateHeadVo(index, OrderDetail.FIELD_AMOUNT, ContextUtil.getMessage("budget_template_amount")));
        } else {
            // 预算类型[{0}]下未找到预算维度
            LOG.error(ContextUtil.getMessage("category_00007", categoryId));
        }
        return list;
    }

    /**
     * 检查是否存在指定类型的预制单
     *
     * @return 返回检查结果
     */
    public List<Order> getPrefabExist(OrderCategory category) {
        Search search = Search.createSearch();
        // 创建人
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
        // 类型
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, category));
        // 预制状态
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB));
        return dao.findByFilters(search);
    }

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId    单据Id
     * @param subjectId  主体id
     * @param categoryId 类型id
     * @return 业务实体
     */
    public ResultData<Void> checkAndGetDimension(String orderId, String subjectId, String categoryId) {
        if (StringUtils.isNotBlank(orderId)) {
            // 通过orderId查询单据
            Order order = dao.findOne(orderId);
            if (Objects.nonNull(order)) {
                OrderDetail detail = orderDetailService.findFirstByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
                if (Objects.nonNull(detail)) {
                    //通过单据保存的主体和类型进行比较,是否一致
                    if (!StringUtils.equals(subjectId, order.getSubjectId())) {
                        // 预算主体不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00002", order.getSubjectName()));
                    }
                    if (!StringUtils.equals(categoryId, order.getCategoryId())) {
                        // 预算类型不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00003", order.getCategoryName()));
                    }
                }
            }
        }
        return ResultData.success();
    }

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId    单据Id
     * @param subjectId  主体id
     * @param categoryId 类型id
     * @return 业务实体
     */
    public ResultData<String> checkDimension(String orderId, String subjectId, String categoryId) {
        if (StringUtils.isNotBlank(orderId)) {
            // 通过orderId查询单据
            Order order = dao.findOne(orderId);
            if (Objects.nonNull(order)) {
                OrderDetail detail = orderDetailService.findFirstByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
                if (Objects.nonNull(detail)) {
                    //通过单据保存的主体和类型进行比较,是否一致
                    if (!StringUtils.equals(subjectId, order.getSubjectId())) {
                        // 预算主体不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00002", order.getSubjectName()));
                    }
                    if (!StringUtils.equals(categoryId, order.getCategoryId())) {
                        // 预算类型不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00003", order.getCategoryName()));
                    }
                }
            }
        }
        return ResultData.success(orderId);
    }

    /**
     * 添加预算申请单行项明细
     *
     * @param orderDto 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<String> addOrderDetails(AddOrderDetail orderDto) {
        if (Objects.isNull(orderDto)) {
            //添加单据行项时,行项数据不能为空.
            return ResultData.fail(ContextUtil.getMessage("order_detail_00004"));
        }
        String categoryId = orderDto.getCategoryId();
        if (StringUtils.isBlank(categoryId)) {
            //添加单据行项时,预算类型不能为空.
            return ResultData.fail(ContextUtil.getMessage("order_detail_00003"));
        }
        // 通过单据Id检查预算主体和类型是否被修改
        ResultData<String> resultData = this.checkDimension(orderDto.getId(), orderDto.getSubjectId(), orderDto.getCategoryId());
        if (resultData.failed()) {
            return resultData;
        }
        Order order = modelMapper.map(orderDto, Order.class);
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        order.setProcessing(Boolean.TRUE);
        // 保存订单头
        ResultData<Order> orderResult = this.saveOrder(order, null);
        if (orderResult.successful()) {
            List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
            if (CollectionUtils.isEmpty(dimensions)) {
                // 预算类型[{0}]下未找到预算维度
                return ResultData.fail(ContextUtil.getMessage("category_00007"));
            }
            String orderId = order.getId();

            List<String> keyList = new ArrayList<>(7);
            // 维度映射
            Map<String, Set<OrderDimension>> dimensionMap = new HashMap<>(7);
            dimensions.parallelStream().forEach(dimension -> {
                String dimensionCode = dimension.getCode();
                keyList.add(dimensionCode);
                // 期间维度
                if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_PERIOD, orderDto.getPeriod());
                }
                // 科目维度
                else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_ITEM, orderDto.getItem());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_ORG, orderDto.getOrg());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_PROJECT, orderDto.getProject());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_COST_CENTER, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_COST_CENTER, orderDto.getCostCenter());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF1, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_UDF1, orderDto.getUdf1());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF2, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_UDF2, orderDto.getUdf2());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF3, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_UDF3, orderDto.getUdf3());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF4, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_UDF4, orderDto.getUdf4());
                } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF5, dimensionCode)) {
                    dimensionMap.put(Constants.DIMENSION_CODE_UDF5, orderDto.getUdf5());
                }
            });

            try {
                List<OrderDetail> detailList = new ArrayList<>();
                OrderDetail detail = new OrderDetail();
                // 订单id
                detail.setOrderId(orderId);

                StopWatch stopWatch = new StopWatch(order.getCode());
                stopWatch.start("笛卡尔方式生成行项");
                // 通过笛卡尔方式生成行项
                this.descartes(keyList, dimensionMap, detailList, 0, detail);
                stopWatch.stop();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(stopWatch.prettyPrint());
                    LOG.debug("生成行项数: " + detailList.size());
                }

                // 保存订单行项.若存在相同的行项则忽略跳过(除非在导入时需要覆盖处理)
                orderDetailService.addOrderItems(order, detailList, Boolean.FALSE);
            } catch (ServiceException e) {
                LOG.error("异步生成单据行项异常", e);
            }
            resultData = ResultData.success(orderId);
        } else {
            resultData = ResultData.fail(orderResult.getMessage());
        }
        return resultData;
    }

    /**
     * 添加预算申请单行项明细(导入使用)
     *
     * @param order        业务实体
     * @param templateHead 模版
     */
    @Transactional(rollbackFor = Exception.class)
    public void importOrderDetails(Order order, List<TemplateHeadVo> templateHead, List<Map<Integer, String>> details) {
        if (Objects.isNull(order)) {
            //导入的订单头数据不能为空
            LOG.error(ContextUtil.getMessage("order_detail_00011"));
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("导入行项数: " + details.size());
        }

        Subject subject = subjectService.getSubject(order.getSubjectId());
        if (Objects.isNull(subject)) {
            LOG.error(ContextUtil.getMessage("subject_00003", order.getSubjectName()));
            return;
        }

        StopWatch stopWatch = new StopWatch("导入处理");
        stopWatch.start("导入数据预处理");
        try {
            Map<String, String> periodMap = new HashMap<>(), subjectItemMap = new HashMap<>(), orgMap = new HashMap<>(),
                    projectMap = new HashMap<>(), costCenterMap = new HashMap<>(),
                    udf1Map = new HashMap<>(), udf2Map = new HashMap<>(), udf3Map = new HashMap<>(), udf4Map = new HashMap<>(), udf5Map = new HashMap<>();
            for (TemplateHeadVo headVo : templateHead) {
                // 期间
                if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                    periodMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_PERIOD));
                }
                // 科目
                else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                    subjectItemMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_ITEM));
                }
                // 组织
                else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                    orgMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_ORG));
                }
                // 项目
                else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                    projectMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_PROJECT));
                }
                // 成本中心
                else if (Constants.DIMENSION_CODE_COST_CENTER.equals(headVo.getFiled())) {
                    costCenterMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_COST_CENTER));
                }
                // 扩展1
                else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
                    udf1Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF1));
                } else if (Constants.DIMENSION_CODE_UDF2.equals(headVo.getFiled())) {
                    udf2Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF2));
                } else if (Constants.DIMENSION_CODE_UDF3.equals(headVo.getFiled())) {
                    udf3Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF3));
                } else if (Constants.DIMENSION_CODE_UDF4.equals(headVo.getFiled())) {
                    udf4Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF4));
                } else if (Constants.DIMENSION_CODE_UDF5.equals(headVo.getFiled())) {
                    udf5Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF5));
                }
            }

            List<OrderDetail> orderDetails = new ArrayList<>();
            LongAdder index = new LongAdder();
            details.parallelStream().forEach(data -> {
                // 第一行为数据头,故跳过
                index.increment();
                if (index.intValue() > 1) {
                    OrderDetail detail = new OrderDetail();
                    String temp;
                    for (TemplateHeadVo headVo : templateHead) {
                        temp = data.get(headVo.getIndex());
                        if (StringUtils.isBlank(temp)) {
                            detail.setHasErr(Boolean.TRUE);
                            // 存在错误的导入数据
                            detail.setErrMsg(ContextUtil.getMessage("order_detail_00023"));
                        } else {
                            // 期间
                            if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                                detail.setPeriodName(temp);
                                String periodId = periodMap.get(temp);
                                if (StringUtils.isNotBlank(periodId)) {
                                    detail.setPeriod(periodId);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的预算期间数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_period")));
                                }
                            }
                            // 科目
                            else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                                detail.setItemName(temp);
                                String itemCode = subjectItemMap.get(temp);
                                if (StringUtils.isNotBlank(itemCode)) {
                                    detail.setItem(itemCode);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的预算科目数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_item")));
                                }
                            }
                            // 组织
                            else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                                detail.setOrgName(temp);
                                String orgId = orgMap.get(temp);
                                if (StringUtils.isNotBlank(orgId)) {
                                    detail.setOrg(orgId);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的组织数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_org")));
                                }
                            }
                            // 项目
                            else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                                detail.setProjectName(temp);
                                String projectId = projectMap.get(temp);
                                if (StringUtils.isNotBlank(projectId)) {
                                    detail.setProject(projectId);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的公司项目数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_project")));
                                }
                            }
                            // 成本中心
                            else if (Constants.DIMENSION_CODE_COST_CENTER.equals(headVo.getFiled())) {
                                detail.setCostCenterName(temp);
                                String costCenter = costCenterMap.get(temp);
                                if (StringUtils.isNotBlank(costCenter)) {
                                    detail.setCostCenter(costCenter);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的成本中心数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_cost_center")));
                                }
                            }
                            // 扩展1
                            else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
                                detail.setUdf1Name(temp);
                                String udf1 = udf1Map.get(temp);
                                if (StringUtils.isNotBlank(udf1)) {
                                    detail.setUdf1(udf1);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的扩展维度1数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf1")));
                                }
                            } else if (Constants.DIMENSION_CODE_UDF2.equals(headVo.getFiled())) {
                                detail.setUdf2Name(temp);
                                String udf2 = udf2Map.get(temp);
                                if (StringUtils.isNotBlank(udf2)) {
                                    detail.setUdf2(udf2);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的扩展维度2数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf2")));
                                }
                            } else if (Constants.DIMENSION_CODE_UDF3.equals(headVo.getFiled())) {
                                detail.setUdf3Name(temp);
                                String udf3 = udf3Map.get(temp);
                                if (StringUtils.isNotBlank(udf3)) {
                                    detail.setUdf3(udf3);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的扩展维度3数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf3")));
                                }
                            } else if (Constants.DIMENSION_CODE_UDF4.equals(headVo.getFiled())) {
                                detail.setUdf4Name(temp);
                                String udf4 = udf4Map.get(temp);
                                if (StringUtils.isNotBlank(udf4)) {
                                    detail.setUdf4(udf4);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的扩展维度4数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf4")));
                                }
                            } else if (Constants.DIMENSION_CODE_UDF5.equals(headVo.getFiled())) {
                                detail.setUdf5Name(temp);
                                String udf5 = udf5Map.get(temp);
                                if (StringUtils.isNotBlank(udf5)) {
                                    detail.setUdf5(udf5);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的扩展维度5数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf5")));
                                }
                            } else if (OrderDetail.FIELD_AMOUNT.equals(headVo.getFiled())) {
                                if (temp.matches(NUM_REGEX)) {
                                    detail.setAmount(new BigDecimal(temp));
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 导入的金额不是数字
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00015"));
                                }
                            }
                        }
                    }
                    orderDetails.add(detail);
                }
            });
            stopWatch.stop();
            stopWatch.start("持久化");
            // 保存订单行项.在导入时,若存在相同的行项则需要覆盖处理
            orderDetailService.addOrderItems(order, orderDetails, Boolean.TRUE);
            stopWatch.stop();
            LOG.info("预算导入处理耗时:\n{}", stopWatch.prettyPrint());
        } catch (ServiceException e) {
            LOG.error("异步导入单据行项异常", e);
        }
    }

    /**
     * 保存预算申请单
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> saveOrder(Order order, List<OrderDetail> details) {
        if (StringUtils.isNotBlank(order.getId())) {
            Order entity = dao.findOne(order.getId());
            if (Objects.nonNull(entity)) {
                order.setCode(entity.getCode());
                order.setCreatorId(entity.getCreatorId());
                order.setCreatorAccount(entity.getCreatorAccount());
                order.setCreatorName(entity.getCreatorName());
                order.setCreatedDate(entity.getCreatedDate());
                order.setApplyAmount(entity.getApplyAmount());
            }
        } else {
            // 生成订单号
            if (StringUtils.isBlank(order.getCode())) {
                order.setCode(serialService.getNumber(Order.class, ContextUtil.getTenantCode()));
            }
        }
        OperateResultWithData<Order> result = this.save(order);
        if (result.successful()) {
            if (CollectionUtils.isNotEmpty(details)) {
                ResultData<Void> resultData = orderDetailService.updateAmount(order, details);
                if (resultData.failed()) {
                    // 回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResultData.fail(resultData.getMessage());
                }
            }
            // 订单总金额
            dao.updateAmount(order.getId());

            try {
                List<String> docIds = order.getDocIds();
                if (Objects.isNull(docIds)) {
                    docIds = new ArrayList<>();
                }
                // 绑定业务实体的文档
                documentManager.bindBusinessDocuments(order.getId(), docIds);
            } catch (Exception e) {
                return ResultData.fail(ContextUtil.getMessage("order_00005"));
            }
            return ResultData.success(order);
        } else {
            return ResultData.fail(result.getMessage());
        }
    }

    /**
     * 更新订单状态
     *
     * @param id     订单id
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, OrderStatus status) {
        dao.updateStatus(id, status);
    }

    /**
     * 更新订单是否正在异步处理行项数据
     * 如果是,在编辑时进入socket状态显示页面
     *
     * @param orderId    订单id
     * @param processing 是否正在异步处理行项数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void setProcessStatus(String orderId, boolean processing) {
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        dao.setProcessStatus(orderId, processing);
    }

    /**
     * 确认预算申请单
     * 预算余额检查并预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> confirm(String orderId) {
        final Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        // 检查订单状态: 状态为草稿和确认中的可进行确认操作
        OrderStatus status = order.getStatus();
        if (OrderStatus.DRAFT == status || OrderStatus.CONFIRMING == status) {
            // 检查是否存在错误行项
            ResultData<Void> resultData = this.checkDetailHasErr(orderId);
            if (resultData.successful()) {
                List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
                if (CollectionUtils.isEmpty(details)) {
                    // 订单[{0}]无行项
                    return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
                }
                // 调整时总额不变(调增调减之和等于0)
                if (OrderCategory.ADJUSTMENT.equals(order.getOrderCategory())) {
                    // 计算调整余额
                    double adjustBalance = details.parallelStream().mapToDouble(detail -> detail.getAmount().doubleValue()).sum();
                    // 检查调整余额是否等于0
                    if (0 != adjustBalance) {
                        // 还有剩余调整余额[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
                    }
                }

                // 更新状态为确认中
                order.setStatus(OrderStatus.CONFIRMING);
                // 更新订单处理状态
                order.setProcessing(Boolean.TRUE);
                orderCommonService.updateOrderStatus(orderId, OrderStatus.CONFIRMING, Boolean.TRUE);

                OrderStatistics statistics = new OrderStatistics(orderId, details.size());
                // 设置默认过期时间:1天
                redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), statistics, 10, TimeUnit.HOURS);

                SessionUser sessionUser = ContextUtil.getSessionUser();
                orderCommonService.asyncConfirm(order, details, sessionUser);

                return ResultData.success(order);
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
    }

    /**
     * 撤销已确认的预算申请单
     * 释放预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> cancelConfirm(String orderId) {
        final Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        OrderStatus status = order.getStatus();
        // 检查订单状态:撤销中的,确认中的,已确认的可进行撤销操作
        if (OrderStatus.CANCELING == status || OrderStatus.CONFIRMING == status || OrderStatus.CONFIRMED == status) {
            List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
            if (CollectionUtils.isEmpty(details)) {
                // 订单[{0}]生效失败: 无订单行项
                return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
            }

            // 更新状态为确认中
            order.setStatus(OrderStatus.CANCELING);
            // 更新订单处理状态
            order.setProcessing(Boolean.TRUE);
            orderCommonService.updateOrderStatus(orderId, OrderStatus.CANCELING, Boolean.TRUE);

            OrderStatistics statistics = new OrderStatistics(orderId, details.size());
            // 设置默认过期时间:1天
            redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), statistics, 10, TimeUnit.HOURS);

            SessionUser sessionUser = ContextUtil.getSessionUser();
            orderCommonService.asyncCancelConfirm(order, details, sessionUser);

            return ResultData.success(order);
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
    }

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> effective(String orderId) {
        final Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        OrderStatus status = order.getStatus();
        // 检查订单状态: 已确认的,审批中的,生效中的可进行生效操作
        if (OrderStatus.CONFIRMED == status || OrderStatus.APPROVING == status || OrderStatus.EFFECTING == status) {
            List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());
            if (CollectionUtils.isEmpty(details)) {
                // 订单[{0}]生效失败: 无订单行项
                return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
            }

            // 检查是否存在错误行项
            ResultData<Void> resultData = this.checkDetailHasErr(orderId);
            if (resultData.successful()) {
                // 调整时总额不变(调增调减之和等于0)
                if (OrderCategory.ADJUSTMENT.equals(order.getOrderCategory())) {
                    // 计算调整余额
                    double adjustBalance = details.parallelStream().mapToDouble(detail -> detail.getAmount().doubleValue()).sum();
                    // 检查调整余额是否等于0
                    if (0 != adjustBalance) {
                        // 还有剩余调整余额[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
                    }
                }

                // 更新状态为生效中
                order.setStatus(OrderStatus.EFFECTING);
                // 更新订单为手动生效标示
                order.setManuallyEffective(Boolean.TRUE);
                // 更新订单处理状态
                order.setProcessing(Boolean.TRUE);
                // 更新订单总金额
                dao.updateAmount(orderId);

                orderCommonService.updateOrderStatus(orderId, OrderStatus.EFFECTING, Boolean.TRUE);

                OrderStatistics statistics = new OrderStatistics(orderId, details.size());
                // 设置默认过期时间:1天
                redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), statistics, 10, TimeUnit.HOURS);

                SessionUser sessionUser = ContextUtil.getSessionUser();
                orderCommonService.asyncEffective(order, details, sessionUser);

                return ResultData.success(order);
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
    }

    /**
     * 检查行项是否有错误未处理
     *
     * @param orderId 预算申请单id
     * @return 返回处理结果
     */
    public ResultData<Void> checkDetailHasErr(String orderId) {
        long hasErrCount = orderDetailService.getHasErrCount(orderId);
        if (hasErrCount == 0) {
            return ResultData.success();
        } else {
            // 存在错误行项未处理
            return ResultData.fail(ContextUtil.getMessage("order_detail_00008"));
        }
    }

    /**
     * 笛卡尔方式生成行项
     */
    private void descartes(List<String> keyList, Map<String, Set<OrderDimension>> dimensionMap,
                           List<OrderDetail> detailList, int layer, OrderDetail detail) {
        // 维度代码
        String dimensionCode = keyList.get(layer);
        // 当前维度所选择的要素清单
        Set<OrderDimension> orderDimensionSet = dimensionMap.get(dimensionCode);
        // 如果不是最后一个子集合时
        if (layer < keyList.size() - 1) {
            // 如果当前子集合元素个数为空，则抛出异常中止
            if (CollectionUtils.isEmpty(orderDimensionSet)) {
                throw new ServiceException("维度[" + dimensionCode + "]未选择要素值.");
            } else {
                OrderDetail od;
                //如果当前子集合元素不为空，则循环当前子集合元素，累加到临时变量。并且继续递归调用，直到达到父集合的最后一个子集合。
                int i = 0;
                for (OrderDimension dimension : orderDimensionSet) {
                    if (i == 0) {
                        od = detail;
                    } else {
                        od = detail.clone();
                    }
                    // 设置维度值
                    setDimension(dimensionCode, dimension, od);
                    descartes(keyList, dimensionMap, detailList, layer + 1, od);
                    i++;
                }
            }
        }
        //递归调用到最后一个子集合时
        else if (layer == keyList.size() - 1) {
            // 如果当前子集合元素为空，则抛出异常中止
            if (CollectionUtils.isEmpty(orderDimensionSet)) {
                throw new ServiceException("维度[" + dimensionCode + "]未选择要素值.");
            } else {
                OrderDetail od;
                //如果当前子集合元素不为空，则循环当前子集合所有元素，累加到临时变量，然后将临时变量加入到结果集中。
                int i = 0;
                for (OrderDimension dimension : orderDimensionSet) {
                    if (i == 0) {
                        od = detail;
                    } else {
                        od = detail.clone();
                    }
                    // 设置维度值
                    setDimension(dimensionCode, dimension, od);
                    detailList.add(od);
                    i++;
                }
            }
        }
    }

    /**
     * 设置维度要素值
     *
     * @param dimensionCode 维度代码
     * @param dimension     选择的维度要素值
     * @param detail        订单行项对象
     */
    private void setDimension(String dimensionCode, OrderDimension dimension, OrderDetail detail) {
        // 期间维度
        if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
            detail.setPeriod(dimension.getValue());
            detail.setPeriodName(dimension.getText());
        }
        // 科目维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
            detail.setItem(dimension.getValue());
            detail.setItemName(dimension.getText());
        }
        // 组织机构维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
            detail.setOrg(dimension.getValue());
            detail.setOrgName(dimension.getText());
        }
        // 项目维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
            detail.setProject(dimension.getValue());
            detail.setProjectName(dimension.getText());
        }
        // 成本中心
        else if (StringUtils.equals(Constants.DIMENSION_CODE_COST_CENTER, dimensionCode)) {
            detail.setCostCenter(dimension.getValue());
            detail.setCostCenterName(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF1, dimensionCode)) {
            detail.setUdf1(dimension.getValue());
            detail.setUdf1Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF2, dimensionCode)) {
            detail.setUdf2(dimension.getValue());
            detail.setUdf2Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF3, dimensionCode)) {
            detail.setUdf3(dimension.getValue());
            detail.setUdf3Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF4, dimensionCode)) {
            detail.setUdf4(dimension.getValue());
            detail.setUdf4Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF5, dimensionCode)) {
            detail.setUdf5(dimension.getValue());
            detail.setUdf5Name(dimension.getText());
        }
    }
}