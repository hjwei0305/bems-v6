package com.changhong.bems.controller;

import com.changhong.bems.api.OrderApi;
import com.changhong.bems.dto.OrderDto;
import com.changhong.bems.entity.Order;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算申请单(Order)控制类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@RestController
@Api(value = "OrderApi", tags = "预算申请单服务")
@RequestMapping(path = OrderApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController extends BaseEntityController<Order, OrderDto> implements OrderApi {
    /**
     * 预算申请单服务对象
     */
    @Autowired
    private OrderService service;

    @Override
    public BaseEntityService<Order> getService() {
        return service;
    }

}