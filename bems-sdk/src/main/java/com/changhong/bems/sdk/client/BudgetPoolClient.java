package com.changhong.bems.sdk.client;

import com.changhong.bems.sdk.dto.BudgetPoolAmountDto;
import com.changhong.bems.sdk.dto.BudgetUseResult;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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
@FeignClient(name = "bems-v6", path = BudgetPoolClient.PATH)
public interface BudgetPoolClient {
    String PATH = "pool";

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCode 预算池code
     * @return 预算池
     */
    @GetMapping(path = "getPoolByCode")
    ResultData<BudgetPoolAmountDto> getPoolByCode(@RequestParam("poolCode") String poolCode);

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCodes 预算池code
     * @return 预算池
     */
    @GetMapping(path = "getPoolsByCode")
    ResultData<List<BudgetPoolAmountDto>> getPoolsByCode(@RequestBody List<String> poolCodes);
}