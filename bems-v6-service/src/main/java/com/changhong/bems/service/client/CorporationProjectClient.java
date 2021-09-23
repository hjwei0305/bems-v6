package com.changhong.bems.service.client;

import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

/**
 * 公司项目(CorporationProject)API
 *
 * @author sei
 * @since 2020-08-17 14:03:30
 */
@Valid
@FeignClient(name = "dms", path = "corporationProject", fallback = CorporationProjectClientFallback.class)
public interface CorporationProjectClient {

    /**
     * 分页获取公司项目
     *
     * @return 未冻结币种清单
     */
    @PostMapping(path = "findByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResultData<PageResult<CorporationProjectDto>> findByPage(Search search);
}