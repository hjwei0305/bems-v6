package com.changhong.bems.api;

import com.changhong.bems.dto.BudgetPoolAmountDto;
import com.changhong.bems.dto.use.BudgetRequest;
import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算(Budget)API
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Valid
@FeignClient(name = "bems-v6", path = BudgetApi.PATH)
public interface BudgetApi {
    String PATH = "budget";

    /**
     * 创建预算池
     *
     * @return 创建结果
     */
    @PostMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建预算池", notes = "创建预算池")
    ResultData<Void> create();

    /**
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    @PostMapping(path = "use", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "使用预算", notes = "使用预算,包含占用和释放")
    ResultData<List<BudgetResponse>> use(@RequestBody @Validated BudgetRequest request);

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
}