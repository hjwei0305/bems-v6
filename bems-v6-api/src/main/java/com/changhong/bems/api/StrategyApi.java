package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算策略(Strategy)API
 *
 * @author sei
 * @since 2021-04-22 11:12:09
 */
@Valid
@FeignClient(name = "bems-v6", path = StrategyApi.PATH)
public interface StrategyApi {
    String PATH = "strategy";

    /**
     * 获取所有预算策略
     *
     * @return 业务实体清单
     */
    @GetMapping(path = "findAll")
    @ApiOperation(value = "获取所有预算策略", notes = "获取所有预算策略")
    ResultData<List<StrategyDto>> findAll();

    /**
     * 按分类查询策略
     *
     * @return 策略清单
     */
    @GetMapping(path = "findByCategory")
    @ApiOperation(value = "按分类查询策略", notes = "按分类查询策略")
    ResultData<List<StrategyDto>> findByCategory(@RequestParam("category") StrategyCategory category);

    /**
     * 按预算维度查询维度策略
     *
     * @return 策略清单
     */
    @GetMapping(path = "findByDimensionCode")
    @ApiOperation(value = "按预算维度查询维度策略", notes = "按预算维度查询维度策略")
    ResultData<List<StrategyDto>> findByDimensionCode(@RequestParam("dimensionCode") String dimensionCode);
}