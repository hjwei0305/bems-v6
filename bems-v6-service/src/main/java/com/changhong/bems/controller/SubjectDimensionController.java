package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectDimensionApi;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.service.StrategyDimensionService;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-02 14:58
 */
@RestController
@Api(value = "SubjectDimensionApi", tags = "预算主体维度策略服务")
@RequestMapping(path = SubjectDimensionApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectDimensionController implements SubjectDimensionApi {
    @Autowired
    private StrategyDimensionService service;

    /**
     * 按预算主体获取维度清单
     *
     * @param subjectId 预算主体id
     * @return 查询结果
     */
    @Override
    public ResultData<List<DimensionDto>> getDimensions(String subjectId) {
        return ResultData.success(service.getDimensions(subjectId));
    }

    /**
     * 设置预算维度为主体私有
     *
     * @param subjectId 预算主体id
     * @param code      预算维度代码
     * @param isPrivate 是否设为私有
     * @return 设置结果
     */
    @Override
    public ResultData<Void> setSubjectDimension(String subjectId, String code, boolean isPrivate) {
        return service.setSubjectDimension(subjectId, code, isPrivate);
    }

    /**
     * 配置预算主体维度策略
     *
     * @param id         id
     * @param strategyId 维度策略
     * @return 配置结果
     */
    @Override
    public ResultData<Void> setDimensionStrategy(String id, String strategyId) {
        return service.setDimensionStrategy(id, strategyId);
    }
}
