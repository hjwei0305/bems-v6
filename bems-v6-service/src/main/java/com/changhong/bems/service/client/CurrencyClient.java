package com.changhong.bems.service.client;

import com.changhong.bems.dto.CurrencyDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * 币种(Currency)API
 *
 * @author sei
 * @since 2020-08-17 14:03:30
 */
@Valid
@FeignClient(name = "dms", path = "currency")
public interface CurrencyClient {

    /**
     * 获取所有未冻结币种
     *
     * @return 未冻结币种清单
     */
    @GetMapping(path = "findAllUnfrozen")
    ResultData<List<CurrencyDto>> findAllUnfrozen();
}