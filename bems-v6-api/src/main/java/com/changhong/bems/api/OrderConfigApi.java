package com.changhong.bems.api;

import com.changhong.bems.dto.OrderConfigDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算配置(Config)API
 *
 * @author sei
 * @since 2021-09-24 09:13:01
 */
@Valid
@FeignClient(name = "bems-v6", path = OrderConfigApi.PATH)
public interface OrderConfigApi {
    String PATH = "orderConfig";

    /**
     * 获取所有订单配置
     *
     * @return 获取所有预算订单配置
     */
    @GetMapping(path = "findAll")
    @ApiOperation(value = "获取所有订单配置", notes = "获取所有预算订单配置")
    ResultData<List<OrderConfigDto>> findAllConfigs();

    /**
     * 订单配置启用
     *
     * @param id     订单配置
     * @param enable 启用状态ø
     * @return 结果
     */
    @PostMapping(path = "updateConfig/{id}")
    @ApiOperation(value = "启用订单配置", notes = "启用指定的订单配置")
    ResultData<Void> updateConfig(@PathVariable("id") String id, @RequestParam("enable") boolean enable);

}