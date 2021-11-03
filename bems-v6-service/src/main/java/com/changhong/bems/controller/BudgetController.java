package com.changhong.bems.controller;

import com.changhong.bems.api.BudgetApi;
import com.changhong.bems.dto.BudgetPoolAmountDto;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.use.BudgetRequest;
import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.bems.service.BudgetService;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.Level;
import com.changhong.sei.core.log.annotation.Log;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
     * 预算池服务对象
     */
    @Autowired
    private PoolService poolService;

    /**
     * 同步预算
     *
     * @return 创建结果
     */
    @Override
    public ResultData<Void> sync() {
        // todo 同步预算
        return null;
    }

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

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCode 预算池代码
     * @return 预算池
     */
    @Override
    public ResultData<BudgetPoolAmountDto> getPoolByCode(String poolCode) {
        ResultData<PoolAttributeDto> resultData = poolService.findPoolAttributeByCode(poolCode);
        if (resultData.successful()) {
            return ResultData.success(this.convertDto(resultData.getData()));
        } else {
            return ResultData.fail(resultData.getMessage());
        }
    }

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCodes 预算池代码
     * @return 预算池
     */
    @Override
    public ResultData<List<BudgetPoolAmountDto>> getPoolsByCode(List<String> poolCodes) {
        List<PoolAttributeDto> attributes = poolService.findPoolAttributes(poolCodes);
        if (CollectionUtils.isNotEmpty(attributes)) {
            List<BudgetPoolAmountDto> results = new ArrayList<>();
            for (PoolAttributeDto attribute : attributes) {
                results.add(this.convertDto(attribute));
            }
            return ResultData.success(results);
        } else {
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    private BudgetPoolAmountDto convertDto(PoolAttributeDto attribute) {
        BudgetPoolAmountDto result = new BudgetPoolAmountDto(attribute.getCode(), attribute.getTotalAmount(), attribute.getUsedAmount(), attribute.getBalance());
        result.setPeriod(attribute.getPeriod());
        result.setPeriodName(attribute.getPeriodName());
        result.setItem(attribute.getItem());
        result.setItemName(attribute.getItemName());
        result.setOrg(attribute.getOrg());
        result.setOrgName(attribute.getOrgName());
        result.setProject(attribute.getProject());
        result.setProjectName(attribute.getProjectName());
        result.setUdf1(attribute.getUdf1());
        result.setUdf1Name(attribute.getUdf1Name());
        result.setUdf2(attribute.getUdf2());
        result.setUdf2Name(attribute.getUdf2Name());
        result.setUdf3(attribute.getUdf3());
        result.setUdf3Name(attribute.getUdf3Name());
        result.setUdf4(attribute.getUdf4());
        result.setUdf4Name(attribute.getUdf4Name());
        result.setUdf5(attribute.getUdf5());
        result.setUdf5Name(attribute.getUdf5Name());
        return result;
    }
}