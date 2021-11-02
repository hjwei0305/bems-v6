package com.changhong.bems.api;

import com.changhong.bems.dto.DimensionDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-02 14:58
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectDimensionApi.PATH)
public interface SubjectDimensionApi {
    String PATH = "subjectDimension";

    /**
     * 按预算主体获取维度清单
     *
     * @return 查询结果
     */
    @GetMapping(path = "getDimensions")
    @ApiOperation(value = "按预算主体获取维度清单", notes = "按预算主体获取维度清单")
    ResultData<List<DimensionDto>> getDimensions(@ApiParam("预算主体id") @RequestParam("subjectId") String subjectId);

    /**
     * 设置预算维度为主体私有
     *
     * @return 设置结果
     */
    @PostMapping(path = "setSubjectDimension")
    @ApiOperation(value = "设置预算维度为主体私有", notes = "设置预算维度为主体私有")
    ResultData<Void> setSubjectDimension(@ApiParam("预算主体id") @RequestParam("subjectId") String subjectId,
                                         @ApiParam("预算维度代码") @RequestParam("code") String code,
                                         @ApiParam("设为主体私有") @RequestParam("isPrivate") boolean isPrivate);

    /**
     * 配置预算主体维度策略
     *
     * @return 配置结果
     */
    @PostMapping(path = "setDimensionStrategy")
    @ApiOperation(value = "配置预算主体维度策略", notes = "配置预算主体维度策略")
    ResultData<Void> setDimensionStrategy(@ApiParam("id") @RequestParam("id") String id,
                                          @ApiParam("维度策略") @RequestParam("strategyId") String strategyId);
}
