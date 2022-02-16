package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyItemDto;
import com.changhong.bems.dto.SubjectItemSearch;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 预算科目(Item)API
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectItemApi.PATH)
public interface SubjectItemApi {
    String PATH = "subjectItem";

    /**
     * 按主体获取预算科目执行策略(预算策略菜单功能使用)
     *
     * @param search search
     * @return 分页查询结果
     */
    @PostMapping(path = "findPageBySubject", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "按主体获取预算科目执行策略", notes = "按主体获取预算科目执行策略")
    ResultData<PageResult<StrategyItemDto>> findPageBySubject(@RequestBody SubjectItemSearch search);
}