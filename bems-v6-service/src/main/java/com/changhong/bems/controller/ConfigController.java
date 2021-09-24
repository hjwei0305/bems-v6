package com.changhong.bems.controller;

import com.changhong.bems.api.OrderConfigApi;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderConfigDto;
import com.changhong.bems.entity.OrderConfig;
import com.changhong.bems.service.OrderConfigService;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算配置(Config)控制类
 *
 * @author sei
 * @since 2021-09-24 09:12:59
 */
@RestController
@Api(value = "com.changhong.bems.api.ConfigApi", tags = "预算配置服务")
@RequestMapping(path = OrderConfigApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigController implements OrderConfigApi {
    /**
     * 预算配置服务对象
     */
    @Autowired
    private OrderConfigService service;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 获取所有订单配置
     *
     * @return 获取所有预算订单配置
     */
    @Override
    public ResultData<List<OrderConfigDto>> findAllConfigs() {
        List<OrderConfig> configs = service.findAllConfigs();
        if (CollectionUtils.isNotEmpty(configs)) {
            return ResultData.success(configs.stream().map(c -> modelMapper.map(c, OrderConfigDto.class)).collect(Collectors.toList()));
        } else {
            return ResultData.success(new ArrayList<>());
        }
    }

    /**
     * 按订单类型获取配置
     *
     * @param category 订单类型
     * @return 按订单类型获取配置
     */
    @Override
    public ResultData<List<OrderConfigDto>> findConfigs(OrderCategory category) {
        List<OrderConfig> configs = service.findByOrderCategory(category);
        if (CollectionUtils.isNotEmpty(configs)) {
            return ResultData.success(configs.stream().map(c -> modelMapper.map(c, OrderConfigDto.class)).collect(Collectors.toList()));
        } else {
            return ResultData.success(new ArrayList<>());
        }
    }

    /**
     * 订单配置启用
     *
     * @param id     订单配置
     * @param enable 启用状态ø
     * @return 结果
     */
    @Override
    public ResultData<Void> updateConfig(String id, boolean enable) {
        return service.updateConfig(id, enable);
    }
}