package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.client.OrganizationManager;
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
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
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
    private OrganizationManager organizationManager;
    @Autowired(required = false)
    private SerialService serialService;
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
        ResultData<String> resultData = this.checkDimension(orderDto.getId(), orderDto.getSubjectId(), categoryId);
        if (resultData.failed()) {
            return resultData;
        }
        Order order = modelMapper.map(orderDto, Order.class);
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        order.setProcessing(Boolean.TRUE);
        // 保存订单头
        ResultData<Order> orderResult = this.saveOrder(order);
        if (orderResult.successful()) {
            List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
            if (CollectionUtils.isEmpty(dimensions)) {
                // 预算类型[{0}]下未找到预算维度
                return ResultData.fail(ContextUtil.getMessage("category_00007"));
            }
            String orderId = order.getId();

            List<String> keyList = Collections.synchronizedList(new ArrayList<>(7));
            // 维度映射
            Map<String, Set<OrderDimension>> dimensionMap = new ConcurrentHashMap<>(7);
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

                orderDetailService.addOrderItems(order, detailList);
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
     * 保存预算申请单
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> saveOrder(Order order) {
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
        if (OrderStatus.DRAFT == status || OrderStatus.APPROVING == status || OrderStatus.EFFECTING == status) {
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