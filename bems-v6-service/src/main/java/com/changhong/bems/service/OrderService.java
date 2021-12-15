package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.*;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.client.CorporationProjectManager;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.mq.BudgetOrderProducer;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.core.util.JsonUtils;
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
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private PeriodService periodService;
    @Autowired
    private SubjectItemService subjectItemService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private OrganizationManager organizationManager;
    @Autowired
    private CorporationProjectManager corporationProjectManager;
    @Autowired(required = false)
    private SerialService serialService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private EventService eventService;
    @Autowired
    private BudgetOrderProducer producer;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DocumentManager documentManager;

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
                if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dto.getCode())) {
                    list.add(new TemplateHeadVo(index++, dto.getCode(), dto.getName().concat("代码")));
                } else {
                    list.add(new TemplateHeadVo(index++, dto.getCode(), dto.getName().concat("ID")));
                }
                list.add(new TemplateHeadVo(index++, dto.getCode().concat("Name"), dto.getName()));
            }
            list.add(new TemplateHeadVo(index, OrderDetail.FIELD_AMOUNT, "金额"));
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
        // 通过单据Id检查预算主体和类型是否被修改
        ResultData<String> resultData = this.checkDimension(orderDto.getId(), orderDto.getSubjectId(), orderDto.getCategoryId());
        if (resultData.failed()) {
            return resultData;
        }
        Order order = modelMapper.map(orderDto, Order.class);
        // 保存订单头
        ResultData<Order> orderResult = this.saveOrder(order, null);
        if (orderResult.successful()) {
            String orderId = order.getId();
            // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
            this.setProcessStatus(orderId, Boolean.TRUE);

            String categoryId = order.getCategoryId();
            if (StringUtils.isBlank(categoryId)) {
                //添加单据行项时,预算类型不能为空.
                return ResultData.fail(ContextUtil.getMessage("order_detail_00003"));
            }
            List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
            if (CollectionUtils.isEmpty(dimensions)) {
                // 预算类型[{0}]下未找到预算维度
                return ResultData.fail(ContextUtil.getMessage("category_00007"));
            }

            List<String> keyList = new ArrayList<>();
            // 维度映射
            Map<String, Set<OrderDimension>> dimensionMap = new HashMap<>(10);
            for (DimensionDto dimension : dimensions) {
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
            }

            try {
                List<OrderDetail> detailList = new ArrayList<>();
                OrderDetail detail = new OrderDetail();
                // 订单id
                detail.setOrderId(orderId);

                // 通过笛卡尔方式生成行项
                descartes(keyList, dimensionMap, detailList, 0, detail);
                if (LOG.isDebugEnabled()) {
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
     * @param orderDto 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<String> importOrderDetails(AddOrderDetail orderDto, List<Map<Integer, String>> details) {
        if (Objects.isNull(orderDto)) {
            //导入的订单头数据不能为空
            return ResultData.fail(ContextUtil.getMessage("order_detail_00011"));
        }
        if (CollectionUtils.isEmpty(details)) {
            //导入的订单行项数据不能为空
            return ResultData.fail(ContextUtil.getMessage("order_detail_00012"));
        }
        Subject subject = subjectService.getSubject(orderDto.getSubjectId());
        if (Objects.isNull(subject)) {
            return ResultData.fail(ContextUtil.getMessage("subject_00003", orderDto.getSubjectName()));
        }
        String categoryId = orderDto.getCategoryId();
        if (StringUtils.isBlank(categoryId)) {
            //添加单据行项时,预算类型不能为空.
            return ResultData.fail(ContextUtil.getMessage("order_detail_00003"));
        }
        Category category = categoryService.findOne(categoryId);
        if (Objects.isNull(category)) {
            //预算类型[{0}]不存在.
            return ResultData.fail(ContextUtil.getMessage("category_00004", categoryId));
        }

        // 通过单据Id检查预算主体和类型是否被修改
        ResultData<String> resultData = this.checkDimension(orderDto.getId(), orderDto.getSubjectId(), categoryId);
        if (resultData.failed()) {
            return resultData;
        }

        // 预算类型获取模版
        List<TemplateHeadVo> templateHead = this.getBudgetTemplate(categoryId);
        // 模版检查
        {
            // 导入模版
            Map<Integer, String> head = details.get(0);
            if (Objects.nonNull(templateHead) && Objects.nonNull(head)
                    && templateHead.size() == head.size()) {
                for (TemplateHeadVo headVo : templateHead) {
                    if (!StringUtils.equals(head.get(headVo.getIndex()), headVo.getValue())) {
                        // 预算数据导入模版不正确
                        return ResultData.fail(ContextUtil.getMessage("order_detail_00014"));
                    }
                }
            } else {
                // 预算数据导入模版不正确
                return ResultData.fail(ContextUtil.getMessage("order_detail_00014"));
            }
        }

        Order order = modelMapper.map(orderDto, Order.class);
        // 保存订单头
        ResultData<Order> orderResult = this.saveOrder(order, null);
        if (orderResult.successful()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("导入行项数: " + details.size());
            }

            String orderId = order.getId();
            // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
            this.setProcessStatus(orderId, Boolean.TRUE);

            // 获取预算期间数据
            List<Period> periods = periodService.findBySubjectUnclosed(order.getSubjectId(), category.getPeriodType());
            Map<String, String> periodMap;
            if (CollectionUtils.isNotEmpty(periods)) {
                periodMap = periods.parallelStream().collect(Collectors.toMap(Period::getId, Period::getName));
                periods.clear();
            } else {
                // 预算期间不存在
                return ResultData.fail(ContextUtil.getMessage("period_00002"));
            }
            // 获取预算科目数据
            List<SubjectItem> subjectItems = subjectItemService.findBySubjectUnfrozen(order.getSubjectId());
            Map<String, String> subjectItemMap;
            if (CollectionUtils.isNotEmpty(periods)) {
                subjectItemMap = subjectItems.parallelStream().collect(Collectors.toMap(SubjectItem::getCode, SubjectItem::getName));
                subjectItems.clear();
            } else {
                // 未找到预算主体[{0}]的可用科目!
                return ResultData.fail(ContextUtil.getMessage("subject_item_00005", order.getSubjectName()));
            }
            // 获取组织数据
            ResultData<List<OrganizationDto>> orgResult = subjectService.getOrgChildren(order.getSubjectId());
            Map<String, String> orgMap;
            if (orgResult.successful()) {
                List<OrganizationDto> orgList = orgResult.getData();
                if (CollectionUtils.isNotEmpty(orgList)) {
                    orgMap = orgList.parallelStream().collect(Collectors.toMap(OrganizationDto::getId, OrganizationDto::getName));
                    orgList.clear();
                } else {
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00021"));
                }
            } else {
                return ResultData.fail(orgResult.getMessage());
            }
            // 获取组织数据
            ResultData<List<ProjectDto>> projectResult = corporationProjectManager.findByErpCode(subject.getCorporationCode());
            Map<String, String> projectMap;
            if (projectResult.successful()) {
                List<ProjectDto> projectList = projectResult.getData();
                if (CollectionUtils.isNotEmpty(projectList)) {
                    projectMap = projectList.parallelStream().collect(Collectors.toMap(ProjectDto::getId, ProjectDto::getName));
                    projectList.clear();
                } else {
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00022"));
                }
            } else {
                return ResultData.fail(orgResult.getMessage());
            }
            try {
                OrderDetail detail;
                List<OrderDetail> orderDetails = new ArrayList<>();
                int index = 0;
                for (Map<Integer, String> data : details) {
                    if (index++ == 0) {
                        // 第一行为数据头,故跳过
                        continue;
                    }

                    detail = new OrderDetail();
                    String temp;
                    for (TemplateHeadVo headVo : templateHead) {
                        temp = data.get(headVo.getIndex());
                        if (StringUtils.isBlank(temp)) {
                            detail.setHasErr(Boolean.TRUE);
                            // 导入的金额不是数字
                            detail.setErrMsg(ContextUtil.getMessage("order_detail_00015"));
                        } else {
                            // 期间
                            if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                                String periodName = periodMap.get(temp);
                                if (StringUtils.isNotBlank(periodName)) {
                                    detail.setPeriod(temp);
                                    detail.setPeriodName(periodName);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的预算期间数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00017"));
                                }
                            }
                            // 科目
                            else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                                String itemName = subjectItemMap.get(temp);
                                if (StringUtils.isNotBlank(itemName)) {
                                    detail.setItem(temp);
                                    detail.setItemName(itemName);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的预算科目数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00020"));
                                }
                            }
                            // 组织
                            else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                                String orgName = orgMap.get(temp);
                                if (StringUtils.isNotBlank(orgName)) {
                                    detail.setOrg(temp);
                                    detail.setOrgName(orgName);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的组织数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00021"));
                                }
                            }
                            // 项目
                            else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                                String projectName = projectMap.get(temp);
                                if (StringUtils.isNotBlank(projectName)) {
                                    detail.setProject(temp);
                                    detail.setProjectName(temp);
                                } else {
                                    detail.setHasErr(Boolean.TRUE);
                                    // 错误的公司项目数据
                                    detail.setErrMsg(ContextUtil.getMessage("order_detail_00022"));
                                }
                            }
                            // 成本中心
                            else if (Constants.DIMENSION_CODE_COST_CENTER.equals(headVo.getFiled())) {
                                detail.setCostCenter(temp);
                            } else if (Constants.DIMENSION_CODE_COST_CENTER.concat("Name").equals(headVo.getFiled())) {
                                detail.setCostCenterName(temp);
                            }
                            // 扩展1
                            else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
                                detail.setUdf1(temp);
                            } else if (Constants.DIMENSION_CODE_UDF1.concat("Name").equals(headVo.getFiled())) {
                                detail.setUdf1Name(temp);
                            } else if (Constants.DIMENSION_CODE_UDF2.equals(headVo.getFiled())) {
                                detail.setUdf2(temp);
                            } else if (Constants.DIMENSION_CODE_UDF2.concat("Name").equals(headVo.getFiled())) {
                                detail.setUdf2Name(temp);
                            } else if (Constants.DIMENSION_CODE_UDF3.equals(headVo.getFiled())) {
                                detail.setUdf3(temp);
                            } else if (Constants.DIMENSION_CODE_UDF3.concat("Name").equals(headVo.getFiled())) {
                                detail.setUdf3Name(temp);
                            } else if (Constants.DIMENSION_CODE_UDF4.equals(headVo.getFiled())) {
                                detail.setUdf4(temp);
                            } else if (Constants.DIMENSION_CODE_UDF4.concat("Name").equals(headVo.getFiled())) {
                                detail.setUdf4Name(temp);
                            } else if (Constants.DIMENSION_CODE_UDF5.equals(headVo.getFiled())) {
                                detail.setUdf5(temp);
                            } else if (Constants.DIMENSION_CODE_UDF5.concat("Name").equals(headVo.getFiled())) {
                                detail.setUdf5Name(temp);
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
                // 保存订单行项.在导入时,若存在相同的行项则需要覆盖处理
                orderDetailService.addOrderItems(order, orderDetails, Boolean.TRUE);
            } catch (ServiceException e) {
                LOG.error("异步导入单据行项异常", e);
            }
            resultData = ResultData.success(orderId);
        } else {
            resultData = ResultData.fail(orderResult.getMessage());
        }
        return resultData;
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

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderId, OrderStatus status, boolean processing) {
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        dao.updateOrderStatus(orderId, status, processing);
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
                    // 订单[{0}]生效失败: 无订单行项
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
                dao.updateOrderStatus(orderId, OrderStatus.CONFIRMING, Boolean.TRUE);
                // 按订单id设置所有行项的处理状态为处理中
                orderDetailService.setProcessing4All(orderId);

                OrderStatistics statistics = new OrderStatistics(orderId, details.size(), LocalDateTime.now());
                BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
                // 设置默认过期时间:1天
                operations.set(statistics, 10, TimeUnit.HOURS);

                // 发送kafka消息
                producer.sendConfirmMessage(orderId, details, ContextUtil.getSessionUser());
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
            dao.updateOrderStatus(orderId, OrderStatus.CANCELING, Boolean.TRUE);
            // 按订单id设置所有行项的处理状态为处理中
            orderDetailService.setProcessing4All(orderId);

            OrderStatistics statistics = new OrderStatistics(orderId, details.size(), LocalDateTime.now());
            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            // 设置默认过期时间:1天
            operations.set(statistics, 10, TimeUnit.HOURS);

            // 发送kafka消息
            producer.sendCancelMessage(orderId, details, ContextUtil.getSessionUser());
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
                dao.save(order);
                // 更新订单总金额
                dao.updateAmount(orderId);
                // 按订单id设置所有行项的处理状态为处理中
                orderDetailService.setProcessing4All(orderId);
                OrderStatistics statistics = new OrderStatistics(orderId, details.size(), LocalDateTime.now());
                BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
                // 设置默认过期时间:1天
                operations.set(statistics, 10, TimeUnit.HOURS);

                // 发送kafka消息
                producer.sendEffectiveMessage(orderId, details, ContextUtil.getSessionUser());
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
     * 确认预算申请单
     * 规则:预算池进行预占用
     *
     * @param detailId 预算申请单行项id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> confirmUseBudget(String detailId) {
        OrderDetail detail = orderDetailService.findOne(detailId);
        if (Objects.isNull(detail)) {
            // 行项不存在
            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
        }
        if (detail.getState() >= 0) {
            // 已处理,不用重复再做预占用
            return ResultData.success();
        }
        String orderId = detail.getOrderId();
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        ResultData<Void> resultData;
        OrderStatus status = order.getStatus();
        // 状态为草稿和确认中的可进行确认操作
        if (OrderStatus.DRAFT == status || OrderStatus.CONFIRMING == status) {
            String poolCode;
            Pool pool = null;
            resultData = ResultData.success();
            String code = order.getCode();
            String remark = order.getRemark();
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        resultData = orderDetailService.checkInjectionDetail(order, detail);
                        if (resultData.failed()) {
                            break;
                        }
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION);
                        }
                        // 记录预算池执行日志.从无到有的新增预算,视为外部注入internal = Boolean.TRUE
                        poolService.poolAmountLog(pool, detailId, code, remark, detail.getAmount(),
                                Constants.EVENT_BUDGET_INJECTION, Boolean.FALSE, OperationType.RELEASE);
                    }
                    break;
                case ADJUSTMENT:
                    poolCode = detail.getPoolCode();
                    if (StringUtils.isNotBlank(poolCode)) {
                        pool = poolService.getPool(poolCode);
                    }
                    if (Objects.isNull(pool)) {
                        LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                        // 预算池不存在
                        resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                        break;
                    }

                    // 为保证性能仅对调减的预算池做额度检查
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        resultData = orderDetailService.checkAdjustmentDetail(order, detail);
                        if (resultData.failed()) {
                            break;
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT);
                        }
                        // 记录预算池执行日志
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount().negate(), Constants.EVENT_BUDGET_ADJUSTMENT, Boolean.TRUE, OperationType.USE);
                    }
                    break;
                case SPLIT:
                    // 预算分解
                    resultData = orderDetailService.checkSplitDetail(order, detail);
                    if (resultData.successful()) {
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT);
                        }

                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            // 当前预算池
                            poolCode = detail.getPoolCode();
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                        } else {
                            // 源预算池
                            String originPoolCode = detail.getOriginPoolCode();
                            if (StringUtils.isNotBlank(originPoolCode)) {
                                pool = poolService.getPool(originPoolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                        }
                    }
                    break;
                default:
                    // 不支持的订单类型
                    resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
            }
            // 标记处理完成
            detail.setProcessing(Boolean.FALSE);
            if (resultData.failed()) {
                detail.setState((short) -1);
                detail.setHasErr(Boolean.TRUE);
                detail.setErrMsg(resultData.getMessage());
            } else {
                // 预占用成功
                detail.setState((short) 0);
            }
            // 更新行项
            orderDetailService.save(detail);
        } else {
            // 可能已发起取消确认动作,故不作任何处理
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
        return resultData;
    }

    /**
     * 取消已确认的预算申请单
     * 规则:释放预占用
     *
     * @param detailId 预算申请单行项id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> cancelConfirmUseBudget(String detailId) {
        OrderDetail detail = orderDetailService.findOne(detailId);
        if (Objects.isNull(detail)) {
            // 行项不存在
            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
        }
        if (detail.getState() < 0) {
            orderDetailService.setProcessed(detailId);
            // 未成功预占用,不用做释放
            return ResultData.success();
        }
        String orderId = detail.getOrderId();
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        ResultData<Void> resultData;
        OrderStatus status = order.getStatus();
        // 撤销中的,确认中的,已确认的可进行撤销操作
        if (OrderStatus.CANCELING == status || OrderStatus.CONFIRMING == status || OrderStatus.CONFIRMED == status) {
            Pool pool = null;
            // 当前预算池
            String poolCode = detail.getPoolCode();
            String code = order.getCode();
            String remark = order.getRemark();
            resultData = ResultData.success();
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION_CANCEL);
                        }
                        // 记录预算池执行日志
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_INJECTION_CANCEL, Boolean.FALSE, OperationType.FREED);
                    }
                    break;
                case ADJUSTMENT:
                    // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT_CANCEL);
                        }
                        // 记录预算池执行日志
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount().negate(), Constants.EVENT_BUDGET_ADJUSTMENT_CANCEL, Boolean.TRUE, OperationType.FREED);
                    }
                    break;
                case SPLIT:
                    // 预算分解
                    if (StringUtils.isBlank(remark)) {
                        remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT_CANCEL);
                    }
                    // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT_CANCEL, Boolean.TRUE, OperationType.FREED);
                        break;
                    } else {
                        // 源预算池
                        String originPoolCode = detail.getOriginPoolCode();
                        if (StringUtils.isNotBlank(originPoolCode)) {
                            pool = poolService.getPool(originPoolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        // 记录预算池执行日志
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT_CANCEL, Boolean.TRUE, OperationType.FREED);
                        break;
                    }
                default:
                    // 不支持的订单类型
                    resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
            }
            // 标记处理完成
            detail.setProcessing(Boolean.FALSE);
            if (resultData.failed()) {
                detail.setHasErr(Boolean.TRUE);
                detail.setErrMsg(resultData.getMessage());
            } else {
                detail.setState((short) -1);
            }
            // 更新行项
            orderDetailService.save(detail);
        } else {
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }

        return resultData;
    }

    /**
     * 流程审批完成生效预算处理
     * 规则:释放预占用,更新正式占用或创建预算池
     *
     * @param detailId 预算申请单行项id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> effectiveUseBudget(String detailId) {
        OrderDetail detail = orderDetailService.findOne(detailId);
        if (Objects.isNull(detail)) {
            // 行项不存在
            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
        }
        if (detail.getState() < 0) {
            // 订单行项未被确认,不能生效
            return ResultData.fail(ContextUtil.getMessage("order_detail_00016"));
        }
        if (detail.getState() > 0) {
            // 已生效的
            return ResultData.success();
        }
        String orderId = detail.getOrderId();
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        ResultData<Void> resultData;
        OrderStatus status = order.getStatus();
        // 已确认的,审批中的,生效中的可进行生效操作
        if (OrderStatus.CONFIRMED == status || OrderStatus.APPROVING == status || OrderStatus.EFFECTING == status) {
            Pool pool = null;
            // 当前预算池
            String poolCode = detail.getPoolCode();
            String code = order.getCode();
            String remark = order.getRemark();
            resultData = ResultData.success();
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) <= 0) {
                        if (StringUtils.isBlank(poolCode)) {
                            // 预算池不存在,需要创建预算池
                            ResultData<Pool> result =
                                    poolService.createPool(order.getSubjectId(), order.getCategoryId(), order.getCurrencyCode(), order.getCurrencyName(),
                                            order.getManagerOrgCode(), order.getManagerOrgName(), order.getPeriodType(), detail, detail.getAmount(), detail.getAmount());
                            if (result.failed()) {
                                resultData = ResultData.fail(result.getMessage());
                                break;
                            }
                            pool = result.getData();
                            poolCode = pool.getCode();
                            detail.setPoolCode(poolCode);
                        } else {
                            pool = poolService.getPool(poolCode);
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION);
                        }
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_INJECTION, Boolean.FALSE, OperationType.RELEASE);
                    }
                    break;
                case ADJUSTMENT:
                    // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) <= 0) {
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT);
                        }
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_ADJUSTMENT, Boolean.TRUE, OperationType.RELEASE);
                    }
                    break;
                case SPLIT:
                    // 预算分解
                    if (StringUtils.isBlank(remark)) {
                        remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT);
                    }
                    if (StringUtils.isBlank(poolCode)) {
                        // 预算池不存在,需要创建预算池
                        ResultData<Pool> result =
                                poolService.createPool(order.getSubjectId(), order.getCategoryId(), order.getCurrencyCode(), order.getCurrencyName(),
                                        order.getManagerOrgCode(), order.getManagerOrgName(), order.getPeriodType(), detail, BigDecimal.ZERO, detail.getAmount());
                        if (result.failed()) {
                            resultData = ResultData.fail(result.getMessage());
                            break;
                        }
                        pool = result.getData();
                        poolCode = pool.getCode();
                        detail.setPoolCode(poolCode);
                    } else {
                        pool = poolService.getPool(poolCode);
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                    }
                    // 源预算池
                    Pool originPool = null;
                    String originPoolCode = detail.getOriginPoolCode();
                    if (StringUtils.isNotBlank(originPoolCode)) {
                        originPool = poolService.getPool(originPoolCode);
                    }
                    if (Objects.isNull(originPool)) {
                        LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                        // 预算池不存在
                        resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                        break;
                    }
                    // 记录预算池执行日志
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        poolService.poolAmountLog(pool, detailId, code, remark,
                                detail.getAmount().negate(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.FREED);

                        poolService.poolAmountLog(pool, detailId, code, ContextUtil.getMessage("order_detail_00018", remark, originPoolCode),
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);

                        poolService.poolAmountLog(originPool, detailId, code, ContextUtil.getMessage("order_detail_00019", remark, poolCode),
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                    } else {
                        poolService.poolAmountLog(pool, detailId, code, ContextUtil.getMessage("order_detail_00018", remark, originPoolCode),
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.RELEASE);

                        // 源预算池
                        poolService.poolAmountLog(originPool, detailId, code, remark,
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.FREED);

                        poolService.poolAmountLog(originPool, detailId, code, ContextUtil.getMessage("order_detail_00019", remark, poolCode),
                                detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                    }
                    break;
                default:
                    // 不支持的订单类型
                    resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
            }
            // 标记处理完成
            detail.setProcessing(Boolean.FALSE);
            if (resultData.failed()) {
                detail.setHasErr(Boolean.TRUE);
                detail.setErrMsg(resultData.getMessage());
            } else {
                // 已生效
                detail.setState((short) 1);
            }
            // 更新行项
            orderDetailService.save(detail);
        } else {
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
        return resultData;
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