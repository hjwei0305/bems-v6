package com.changhong.bems.controller;

import com.changhong.bems.api.PoolApi;
import com.changhong.bems.dto.PoolDto;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算池(Pool)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@RestController
@Api(value = "PoolApi", tags = "预算池服务")
@RequestMapping(path = PoolApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class PoolController extends BaseEntityController<Pool, PoolDto> implements PoolApi {
    /**
     * 预算池服务对象
     */
    @Autowired
    private PoolService service;

    @Override
    public BaseEntityService<Pool> getService() {
        return service;
    }

    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<PoolDto>> findByPage(Search search) {
        return convertToDtoPageResult(service.findByPage(search));
    }
}