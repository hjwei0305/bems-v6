package com.changhong.bems.api;

import com.changhong.bems.dto.BudgetPoolAmountDto;
import com.changhong.bems.dto.LogRecordDto;
import com.changhong.bems.dto.PoolAttributeDto;
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
import java.util.List;
import java.util.Set;

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
     * 通过预算池代码获取一个预算池
     *
     * @param poolCode 预算池code
     * @return 预算池
     */
    @GetMapping(path = "getPoolByCode")
    @ApiOperation(value = "通过代码获取一个预算池", notes = "通过预算池代码获取一个预算池")
    ResultData<BudgetPoolAmountDto> getPoolByCode(@RequestParam("poolCode") String poolCode);

    /**
     * 通过预算池代码获取预算池
     *
     * @param poolCodes 预算池code清单
     * @return 预算池
     */
    @PostMapping(path = "getPoolsByCode")
    @ApiOperation(value = "通过代码获取预算池", notes = "通过预算池代码获取预算池")
    ResultData<List<BudgetPoolAmountDto>> getPoolsByCode(@RequestBody List<String> poolCodes);

    /**
     * 分页查询预算池
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询预算池", notes = "分页查询预算池")
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

    /**
     * 通过Id启用预算池
     *
     * @param ids 预算池Id集合
     * @return 启用结果
     */
    @PostMapping(path = "enable", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过Id启用预算池", notes = "通过Id启用预算池")
    ResultData<Void> enable(@RequestBody Set<String> ids);

    /**
     * 通过Id禁用预算池
     *
     * @param ids 预算池Id集合
     * @return 启用结果
     */
    @PostMapping(path = "disable", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过Id禁用预算池", notes = "通过Id禁用预算池")
    ResultData<Void> disable(@RequestBody Set<String> ids);

    /**
     * 滚动预算池
     *
     * @param id 预算池id
     * @return 滚动结果
     */
    @PostMapping(path = "trundle", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "滚动预算池", notes = "通过Id滚动预算池")
    ResultData<Void> trundlePool(@RequestParam("id") String id);

    /**
     * 分页查询预算执行日志
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findRecordByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询预算执行日志", notes = "分页查询预算执行日志")
    ResultData<PageResult<LogRecordDto>> findRecordByPage(@RequestBody Search search);

}