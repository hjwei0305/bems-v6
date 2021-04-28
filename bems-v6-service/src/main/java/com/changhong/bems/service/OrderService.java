package com.changhong.bems.service;

import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.OrderDetailDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
    @Autowired
    private OrderDao dao;
    @Autowired
    private OrganizationManager organizationManager;

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
     * @param search
     * @return 业务实体
     */
    public PageResult<OrderDetail> getOrderItems(String orderId, Search search) {
        return null;
    }
}