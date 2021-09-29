package com.changhong.bems.api;

import com.changhong.bems.dto.use.BudgetRequest;
import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    @PostMapping(path = "use", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "使用预算", notes = "使用预算,包含占用和释放")
    ResultData<List<BudgetResponse>> use(@RequestBody @Validated BudgetRequest request);

}