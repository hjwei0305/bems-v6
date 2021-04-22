package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
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
public interface StrategyApi extends BaseEntityApi<StrategyDto>, FindAllApi<StrategyDto> {
    String PATH = "strategy";

    /**
     * 按分类查询策略
     *
     * @return 策略清单
     */
    @GetMapping(path = "findByCategory")
    @ApiOperation(value = "按分类查询策略", notes = "按分类查询策略")
    ResultData<List<StrategyDto>> findByCategory(@RequestParam("category") String category);
}