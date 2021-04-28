package com.changhong.bems.service;

import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预算维度属性(OrderDetail)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Service
public class OrderDetailService extends BaseEntityService<OrderDetail> {
    @Autowired
    private OrderDetailDao dao;

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
}