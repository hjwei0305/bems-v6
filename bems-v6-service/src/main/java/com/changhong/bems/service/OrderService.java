package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.vo.TemplateHeadVo;
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
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.serial.sdk.SerialService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

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
    private OrganizationManager organizationManager;
    @Autowired(required = false)
    private SerialService serialService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private BudgetOrderProducer producer;

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
    public Order getPrefabExist(OrderCategory category) {
        Search search = Search.createSearch();
        // 创建人
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
        // 类型
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, category));
        // 预制状态
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB));
        return dao.findFirstByFilters(search);
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
                            if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                                detail.setPeriod(temp);
                            } else if (Constants.DIMENSION_CODE_PERIOD.concat("Name").equals(headVo.getFiled())) {
                                detail.setPeriodName(temp);
                            } else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                                detail.setItem(temp);
                            } else if (Constants.DIMENSION_CODE_ITEM.concat("Name").equals(headVo.getFiled())) {
                                detail.setItemName(temp);
                            } else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                                detail.setOrg(temp);
                            } else if (Constants.DIMENSION_CODE_ORG.concat("Name").equals(headVo.getFiled())) {
                                detail.setOrgName(temp);
                            } else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                                detail.setProject(temp);
                            } else if (Constants.DIMENSION_CODE_PROJECT.concat("Name").equals(headVo.getFiled())) {
                                detail.setProjectName(temp);
                            } else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
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
                                if (StringUtils.isNumeric(temp)) {
                                    detail.setAmount(Double.valueOf(temp));
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
        if (StringUtils.isBlank(order.getId())) {
            order.setCode(serialService.getNumber(Order.class, ContextUtil.getTenantCode()));
        } else {
            Order entity = dao.findOne(order.getId());
            if (Objects.nonNull(entity)) {
                order.setCode(entity.getCode());
                order.setCreatorId(entity.getCreatorId());
                order.setCreatorAccount(entity.getCreatorAccount());
                order.setCreatorName(entity.getCreatorName());
                order.setCreatedDate(entity.getCreatedDate());
                order.setApplyAmount(entity.getApplyAmount());
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
     * 更新订单总金额
     *
     * @param id 订单id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAmount(String id) {
        dao.updateAmount(id);
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
    public ResultData<Void> confirm(String orderId) {
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
                    double adjustBalance = details.parallelStream().mapToDouble(OrderDetail::getAmount).sum();
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
                dao.save(order);

                // 发送kafka消息
                producer.sendConfirmMessage(orderId, details, ContextUtil.getSessionUser());
                resultData = ResultData.success();
            }
            return resultData;
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
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
    public ResultData<Void> cancelConfirm(String orderId) {
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
            dao.save(order);

            // 发送kafka消息
            producer.sendCancelMessage(orderId, details, ContextUtil.getSessionUser());
            return ResultData.success();
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> effective(String orderId) {
        final Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        OrderStatus status = order.getStatus();
        // 检查订单状态: 已确认的,审批中的,生效中的可进行生效操作
        if (OrderStatus.CONFIRMED == status || OrderStatus.APPROVAL == status || OrderStatus.EFFECTING == status) {
            List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());
            if (CollectionUtils.isEmpty(details)) {
                // 订单[{0}]生效失败: 无订单行项
                return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
            }
            // 更新状态为生效中
            order.setStatus(OrderStatus.EFFECTING);
            // 更新订单为手动生效标示
            order.setManuallyEffective(Boolean.TRUE);
            dao.save(order);
            // 更新订单总金额
            dao.updateAmount(orderId);
            // 检查是否存在错误行项
            ResultData<Void> resultData = this.checkDetailHasErr(orderId);
            if (resultData.successful()) {
                // 调整时总额不变(调增调减之和等于0)
                if (OrderCategory.ADJUSTMENT.equals(order.getOrderCategory())) {
                    // 计算调整余额
                    double adjustBalance = details.parallelStream().mapToDouble(OrderDetail::getAmount).sum();
                    // 检查调整余额是否等于0
                    if (0 != adjustBalance) {
                        // 还有剩余调整余额[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
                    }
                }
                // 发送kafka消息
                producer.sendEffectiveMessage(orderId, details, ContextUtil.getSessionUser());
                resultData = ResultData.success();
            }
            return resultData;
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
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

//    /**
//     * 直接生效预算处理
//     * 规则:更新或创建预算池
//     *
//     * @param detailId 预算申请单行项id
//     * @return 返回处理结果
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public ResultData<Void> effectiveUseBudget(String detailId) {
//        OrderDetail detail = orderDetailService.findOne(detailId);
//        if (Objects.isNull(detail)) {
//            // 行项不存在
//            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
//        }
//        String orderId = detail.getOrderId();
//        Order order = dao.findOne(orderId);
//        if (Objects.isNull(order)) {
//            // 订单不存在
//            return ResultData.fail(ContextUtil.getMessage("order_00001"));
//        }
//        // 更新行项的处理状态为处理完成
//        orderDetailService.setProcessed(detailId);
//        if (OrderStatus.CONFIRMED == order.getStatus()) {
//            String remark;
//            String poolCode;
//            ExecutionRecord record;
//            ResultData<Void> resultData = ResultData.success();
//            OperationType operation = OperationType.RELEASE;
//            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
//            switch (order.getOrderCategory()) {
//                case INJECTION:
//                    if (detail.getAmount() < 0) {
//                        resultData = orderDetailService.checkInjectionDetail(order, detail);
//                        if (resultData.failed()) {
//                            break;
//                        }
//                    }
//                    poolCode = detail.getPoolCode();
//                    if (StringUtils.isBlank(poolCode)) {
//                        // 预算池不存在,需要创建预算池
//                        ResultData<Pool> result = poolService.createPool(order, detail);
//                        if (result.failed()) {
//                            resultData = ResultData.fail(result.getMessage());
//                            break;
//                        }
//                        Pool pool = result.getData();
//                        poolCode = pool.getCode();
//                        detail.setPoolCode(poolCode);
//                        detail.setPoolAmount(pool.getBalance());
//                    }
//                    // 记录预算池执行日志
//                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_EFFECTIVE);
//                    record.setSubjectId(order.getSubjectId());
//                    record.setAttributeCode(detail.getAttributeCode());
//                    record.setBizCode(order.getCode());
//                    record.setBizId(detail.getId());
//                    remark = order.getRemark();
//                    record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
//                    poolService.recordLog(record);
//                    break;
//                case ADJUSTMENT:
//                    resultData = orderDetailService.checkAdjustmentDetail(order, detail);
//                    if (resultData.failed()) {
//                        break;
//                    }
//                    poolCode = detail.getPoolCode();
//                    // 记录预算池执行日志
//                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_EFFECTIVE);
//                    record.setSubjectId(order.getSubjectId());
//                    record.setAttributeCode(detail.getAttributeCode());
//                    record.setBizCode(order.getCode());
//                    record.setBizId(detail.getId());
//                    remark = order.getRemark();
//                    record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
//                    poolService.recordLog(record);
//                    break;
//                case SPLIT:
//                    // 预算分解
//                    resultData = orderDetailService.checkSplitDetail(order, detail);
//                    if (resultData.successful()) {
//                        // 当前预算池
//                        poolCode = detail.getPoolCode();
//                        if (StringUtils.isBlank(poolCode)) {
//                            // 预算池不存在,需要创建预算池
//                            ResultData<Pool> result = poolService.createPool(order, detail);
//                            if (result.failed()) {
//                                resultData = ResultData.fail(result.getMessage());
//                                break;
//                            }
//                            Pool pool = result.getData();
//                            poolCode = pool.getCode();
//                            detail.setPoolCode(poolCode);
//                            detail.setPoolAmount(pool.getBalance());
//                        }
//                        // 记录预算池执行日志
//                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_EFFECTIVE);
//                        record.setSubjectId(order.getSubjectId());
//                        record.setAttributeCode(detail.getAttributeCode());
//                        record.setBizCode(order.getCode());
//                        record.setBizId(detail.getId());
//                        remark = order.getRemark();
//                        record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
//                        poolService.recordLog(record);
//                        // 源预算池
//                        String originPoolCode = detail.getOriginPoolCode();
//                        // 记录预算池执行日志
//                        record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_EFFECTIVE);
//                        record.setSubjectId(order.getSubjectId());
//                        record.setAttributeCode(detail.getAttributeCode());
//                        record.setBizCode(order.getCode());
//                        record.setBizId(detail.getId());
//                        remark = order.getRemark();
//                        record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
//                        poolService.recordLog(record);
//                        break;
//                    } else {
//                        break;
//                    }
//                default:
//                    // 不支持的订单类型
//                    resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
//            }
//            if (resultData.successful()) {
//                // 更新订单状态为:完成
//                long processingCount = orderDetailService.getProcessingCount(orderId);
//                if (processingCount == 0) {
//                    dao.updateStatus(orderId, OrderStatus.COMPLETED);
//                }
//            } else {
//                // 生效失败,更新订单状态为:部分完成
//                dao.updateStatus(orderId, OrderStatus.PARTIALLY_COMPLETED);
//            }
//            return resultData;
//        } else {
//            // 订单状态为[{0}],不允许操作!
//            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
//        }
//    }

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
            String remark;
            String poolCode;
            ExecutionRecord record;
            resultData = ResultData.success();
            OperationType operation = OperationType.RELEASE;
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    if (detail.getAmount() < 0) {
                        resultData = orderDetailService.checkInjectionDetail(order, detail);
                        if (resultData.failed()) {
                            break;
                        }
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isBlank(poolCode)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池金额不能值为负数[{0}]
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                            break;
                        }
                    } else {
                        poolCode = detail.getPoolCode();
                    }

                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_SUBMIT);
                    if (detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("检查确认-" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case ADJUSTMENT:
                    // 为保证性能仅对调减的预算池做额度检查
                    if (detail.getAmount() < 0) {
                        resultData = orderDetailService.checkAdjustmentDetail(order, detail);
                        if (resultData.failed()) {
                            break;
                        }
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isBlank(poolCode)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池金额不能值为负数[{0}]
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                            break;
                        }
                    } else {
                        poolCode = detail.getPoolCode();
                    }

                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_SUBMIT);
                    if (detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("检查确认" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case SPLIT:
                    // 预算分解
                    resultData = orderDetailService.checkSplitDetail(order, detail);
                    if (resultData.successful()) {
                        // 当前预算池
                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_SUBMIT);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("检查确认" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);

                        // 源预算池
                        String originPoolCode = detail.getOriginPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_SUBMIT);
                        if (-detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("检查确认" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    }
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
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        // 获取处理中的订单行项数.等于0表示处理完订单所有行项
        long processingCount = orderDetailService.getProcessingCount(orderId);
        if (processingCount == 0) {
            // 更新订单状态为:已确认
            dao.updateStatus(orderId, OrderStatus.CONFIRMED);
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
            String remark;
            String poolCode;
            ExecutionRecord record;
            resultData = ResultData.success();
            OperationType operation = OperationType.RELEASE;
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    poolCode = detail.getPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_INJECTION_CANCEL);
                    if (detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("撤销确认 " + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case ADJUSTMENT:
                    poolCode = detail.getPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_ADJUSTMENT_CANCEL);
                    if (detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("撤销确认 " + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case SPLIT:
                    // 预算分解
                    // 当前预算池
                    poolCode = detail.getPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_CANCEL);
                    if (detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("撤销确认 " + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);

                    // 源预算池
                    String originPoolCode = detail.getOriginPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(originPoolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_CANCEL);
                    if (-detail.getAmount() >= 0) {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("撤销确认 " + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
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
                detail.setState((short) -1);
            }
            // 更新行项
            orderDetailService.save(detail);
        } else {
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        // 获取处理中的订单行项数.等于0表示处理完订单所有行项
        long processingCount = orderDetailService.getProcessingCount(orderId);
        if (processingCount == 0) {
            // 更新订单状态为:已确认
            dao.updateStatus(orderId, OrderStatus.DRAFT);
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
        if (detail.getState() >= 1) {
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
        // 已确认的,审批中的,生效中的可进行生效操作
        if (OrderStatus.CONFIRMED == status || OrderStatus.APPROVAL == status || OrderStatus.EFFECTING == status) {
            String remark;
            String poolCode;
            ExecutionRecord record;
            resultData = ResultData.success();
            OperationType operation = OperationType.RELEASE;
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    poolCode = detail.getPoolCode();
                    if (StringUtils.isBlank(poolCode)) {
                        // 预算池不存在,需要创建预算池
                        ResultData<Pool> result = poolService.createPool(order, detail);
                        if (result.failed()) {
                            return ResultData.fail(result.getMessage());
                        }
                        Pool pool = result.getData();
                        poolCode = pool.getCode();
                    }
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_COMPLETE);
                    if (detail.getAmount() <= 0) {
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("预算生效" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case ADJUSTMENT:
                    poolCode = detail.getPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_COMPLETE);
                    if (detail.getAmount() <= 0) {
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("预算生效" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                case SPLIT:
                    // 预算分解
                    // 当前预算池
                    poolCode = detail.getPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_COMPLETE);
                    if (detail.getAmount() <= 0) {
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("预算生效" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);

                    // 源预算池
                    String originPoolCode = detail.getOriginPoolCode();
                    // 记录预算池执行日志
                    record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_COMPLETE);
                    if (-detail.getAmount() <= 0) {
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        record.setIsPoolAmount(Boolean.FALSE);
                    }
                    record.setSubjectId(order.getSubjectId());
                    record.setAttributeCode(detail.getAttributeCode());
                    record.setBizCode(order.getCode());
                    record.setBizId(detail.getId());
                    remark = order.getRemark();
                    record.setBizRemark("预算生效" + (StringUtils.isBlank(remark) ? "" : remark));
                    poolService.recordLog(record);
                    break;
                default:
                    // 不支持的订单类型
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
            }
            // 标记处理完成
            detail.setProcessing(Boolean.FALSE);
            if (resultData.failed()) {
                detail.setHasErr(Boolean.TRUE);
                detail.setErrMsg(resultData.getMessage());
            }
            // 更新行项
            orderDetailService.save(detail);
        } else {
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        // 获取处理中的订单行项数.等于0表示处理完订单所有行项
        long processingCount = orderDetailService.getProcessingCount(orderId);
        if (processingCount == 0) {
            // 更新订单状态为:已生效
            dao.updateStatus(orderId, OrderStatus.COMPLETED);
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