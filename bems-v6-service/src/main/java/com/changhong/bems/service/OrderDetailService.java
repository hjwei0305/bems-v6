package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.OrderDimension;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 预算维度属性(OrderDetail)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Service
public class OrderDetailService extends BaseEntityService<OrderDetail> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailService.class);

    @Autowired
    private OrderDetailDao dao;
    @Autowired
    private CategoryService categoryService;

    @Override
    protected BaseEntityDao<OrderDetail> getDao() {
        return dao;
    }

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearOrderItems(String orderId) {
        int count = dao.clearOrderItems(orderId);
        if (LogUtil.isDebugEnabled()) {
            LogUtil.debug("预算申请单[" + orderId + "]清空明细[" + count + "]行.");
        }
    }

    /**
     * 异步生成单据行项
     * 若存在相同的行项则忽略跳过(除非在导入时需要覆盖处理)
     *
     * @param orderId    单据Id
     * @param categoryId 预算类型id
     */
    @Async
    public void batchAddOrderItems(String orderId, String categoryId, AddOrderDetail addOrderDetail) {
        if (StringUtils.isBlank(orderId)) {
            //添加单据行项时,订单id不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
        }
        if (StringUtils.isBlank(categoryId)) {
            //添加单据行项时,预算类型不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00002"));
        }
        if (Objects.isNull(addOrderDetail)) {
            //添加单据行项时,行项数据不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00003"));
        }
        List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
        if (CollectionUtils.isEmpty(dimensions)) {
            // 预算类型[{0}]下未找到预算维度
            LOG.error(ContextUtil.getMessage("category_00007"));
        }
        List<String> keyList = new ArrayList<>();
        // 维度映射
        Map<String, Set<OrderDimension>> dimensionMap = new HashMap<>();
        for (DimensionDto dimension : dimensions) {
            String dimensionCode = dimension.getCode();
            keyList.add(dimensionCode);
            // 期间维度
            if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_PERIOD, addOrderDetail.getPeriod());
            }
            // 科目维度
            else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_ITEM, addOrderDetail.getItem());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_ORG, addOrderDetail.getOrg());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_PROJECT, addOrderDetail.getProject());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF1, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF1, addOrderDetail.getUdf1());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF2, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF2, addOrderDetail.getUdf2());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF3, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF3, addOrderDetail.getUdf3());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF4, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF4, addOrderDetail.getUdf4());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF5, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF5, addOrderDetail.getUdf5());
            }
        }

        try {
            List<OrderDetail> detailList = new ArrayList<>();
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);

            // 通过笛卡尔方式生成行项
            descartes(keyList, dimensionMap, detailList, 0, detail);
            if (LOG.isDebugEnabled()) {
                LOG.debug("生成行项: " + detailList.size());
            }

            // TODO 检查重复行项: 存在重复时,忽略跳过

            // TODO 获取预算池及可用额度

            this.save(detailList);
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        }
    }

    /**
     * 笛卡尔方式生成行项
     */
    private static void descartes(List<String> keyList, Map<String, Set<OrderDimension>> dimensionMap,
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
    private static void setDimension(String dimensionCode, OrderDimension dimension, OrderDetail detail) {
        // 期间维度
        if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
            detail.setPeriod(dimension.getValue());
            detail.setPeriodName(dimension.getText());
        }
        // 科目维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
            detail.setItem(dimension.getValue());
            detail.setItemName(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
            detail.setOrg(dimension.getValue());
            detail.setOrgName(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
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