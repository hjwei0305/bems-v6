package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyItemDto;
import com.changhong.bems.dto.SubjectItemSearch;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * 预算科目(Item)API
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Valid
@FeignClient(name = "bems-v6", path = StrategyItemApi.PATH)
public interface StrategyItemApi {
    String PATH = "strategyItem";

    /**
     * 按主体获取预算科目执行策略(预算策略菜单功能使用)
     *
     * @param search search
     * @return 分页查询结果
     */
    @PostMapping(path = "findPageBySubject", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "按主体获取预算科目执行策略", notes = "按主体获取预算科目执行策略")
    ResultData<PageResult<StrategyItemDto>> findPageBySubject(@RequestBody SubjectItemSearch search);

    /**
     * 设置预算科目为主体私有
     *
     * @return 设置结果
     */
    @PostMapping(path = "turnPrivate")
    @ApiOperation(value = "设置预算科目为主体私有", notes = "设置预算科目为主体私有")
    ResultData<Void> turnPrivate(@ApiParam("预算主体id") @RequestParam("subjectId") String subjectId,
                                 @ApiParam("预算科目代码") @RequestParam("itemCode") String itemCode,
                                 @ApiParam("设为主体私有") @RequestParam("isPrivate") boolean isPrivate);

    /**
     * 配置预算科目执行策略
     *
     * @return 配置结果
     */
    @PostMapping(path = "setStrategy")
    @ApiOperation(value = "配置预算科目执行策略", notes = "配置预算科目执行策略")
    ResultData<Void> setStrategy(@ApiParam("预算主体id") @RequestParam("subjectId") String subjectId,
                                 @ApiParam("预算科目代码") @RequestParam("itemCode") String itemCode,
                                 @ApiParam("维度策略") @RequestParam("strategyId") String strategyId);
}