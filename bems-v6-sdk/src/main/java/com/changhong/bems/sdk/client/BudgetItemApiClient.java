package com.changhong.bems.sdk.client;

import com.changhong.bems.dto.BudgetItemDto;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-17 13:09
 */
@Valid
@FeignClient(name = "bems-v6", path = BudgetItemApiClient.PATH)
public interface BudgetItemApiClient {
    String PATH = "item";

    /**
     * 分页获取预算科目(外部系统集成专用)
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "getBudgetItems", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResultData<PageResult<BudgetItemDto>> getBudgetItems(@RequestBody Search search);
}
