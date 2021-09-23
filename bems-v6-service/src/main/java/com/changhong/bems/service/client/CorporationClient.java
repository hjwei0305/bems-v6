package com.changhong.bems.service.client;

import com.changhong.bems.dto.CorporationDto;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 实现功能：公司接口api
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 17:32
 */
@FeignClient(name = "sei-basic", path = "corporation", fallback = CorporationClientFallback.class)
public interface CorporationClient {

    /**
     * 获取当前用户有权限的公司清单(未冻结)
     *
     * @param featureCode 功能项代码
     * @return 有权限的公司数据清单
     */
    @GetMapping(path = "getUserAuthorizedEntities")
    ResultData<List<CorporationDto>> getUserAuthorizedEntities(@RequestParam(value = "featureCode", required = false, defaultValue = "") String featureCode);
}
