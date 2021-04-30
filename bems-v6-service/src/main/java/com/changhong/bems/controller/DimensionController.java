package com.changhong.bems.controller;

import com.changhong.bems.api.DimensionApi;
import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.service.DimensionService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 预算维度(Dimension)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@RestController
@Api(value = "DimensionApi", tags = "预算维度服务")
@RequestMapping(path = DimensionApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class DimensionController extends BaseEntityController<Dimension, DimensionDto> implements DimensionApi {
    /**
     * 预算维度服务对象
     */
    @Autowired
    private DimensionService service;

    @Override
    public BaseEntityService<Dimension> getService() {
        return service;
    }

    /**
     * 获取所有业务实体
     *
     * @return 业务实体清单
     */
    @Override
    public ResultData<List<DimensionDto>> findAll() {
        return ResultData.success(convertToDtos(service.findAll()));
    }

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    @Override
    public ResultData<Set<KeyValueDto>> findAllCodes() {
        return ResultData.success(Constants.getDimensionCodes());
    }
}