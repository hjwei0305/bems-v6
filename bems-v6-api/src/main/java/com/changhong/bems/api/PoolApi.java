package com.changhong.bems.api;

import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * 预算池(Pool)API
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Valid
@FeignClient(name = "bems-v6", path = PoolApi.PATH)
public interface PoolApi {
    String PATH = "pool";

    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询业务实体", notes = "分页查询业务实体")
    ResultData<PageResult<PoolAttributeDto>> findByPage(@RequestBody Search search);

    /**
     * 通过Id获取一个预算池
     *
     * @param id 预算池Id
     * @return 预算池
     */
    @GetMapping(path = "getPool")
    @ApiOperation(value = "获取一个预算池", notes = "通过Id获取一个预算池")
    ResultData<PoolAttributeDto> getPool(@RequestParam("id") String id);
}