package com.changhong.bems.controller;

import com.changhong.bems.api.StrategyApi;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.bems.entity.Strategy;
import com.changhong.bems.service.StrategyService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (Strategy)控制类
 *
 * @author sei
 * @since 2021-04-22 11:12:06
 */
@RestController
@Api(value = "StrategyApi", tags = "服务")
@RequestMapping(path = StrategyApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class StrategyController extends BaseEntityController<Strategy, StrategyDto> implements StrategyApi {
    /**
     * 服务对象
     */
    @Autowired
    private StrategyService service;

    @Override
    public BaseEntityService<Strategy> getService() {
        return service;
    }

    /**
     * 获取所有业务实体
     *
     * @return 业务实体清单
     */
    @Override
    public ResultData<List<StrategyDto>> findAll() {
        return ResultData.success(convertToDtos(service.findAll()));
    }

    /**
     * 获取所有未冻结的业务实体
     *
     * @return 业务实体清单
     */
    @Override
    public ResultData<List<StrategyDto>> findAllUnfrozen() {
        return ResultData.success(convertToDtos(service.findAllUnfrozen()));
    }
}