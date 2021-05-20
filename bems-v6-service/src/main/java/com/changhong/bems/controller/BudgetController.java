package com.changhong.bems.controller;

import com.changhong.bems.api.BudgetApi;
import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.service.BudgetService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.Level;
import com.changhong.sei.core.log.annotation.Log;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预算(Budget)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@RestController
@Api(value = "BudgetApi", tags = "预算使用服务")
@RequestMapping(path = BudgetApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class BudgetController implements BudgetApi {

    /**
     * 预算服务对象
     */
    @Autowired
    private BudgetService service;

    /**
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    @Override
    @Log(value = "使用预算", level = Level.INFO)
    public ResultData<List<BudgetResponse>> use(BudgetRequest request) {
        return service.use(request);
    }
}