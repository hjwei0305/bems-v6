package com.changhong.bems.api;

import com.changhong.bems.dto.SubjectPeriodDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算期间策略(SubjectPeriod)API
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectPeriodApi.PATH)
public interface SubjectPeriodApi extends BaseEntityApi<SubjectPeriodDto> {
    String PATH = "subjectPeriod";

    /**
     * 冻结预算期间策略
     *
     * @param ids 预算期间策略id
     * @return 操作结果
     */
    @PostMapping(path = "frozen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "冻结预算期间策略", notes = "冻结预算期间策略")
    ResultData<Void> frozen(@RequestBody List<String> ids);

    /**
     * 解冻预算期间策略
     *
     * @param ids 预算期间策略id
     * @return 操作结果
     */
    @PostMapping(path = "unfrozen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "解冻预算期间策略", notes = "解冻预算期间策略")
    ResultData<Void> unfrozen(@RequestBody List<String> ids);

    /**
     * 获取指定主体的预算期间策略
     *
     * @return 子实体清单
     */
    @GetMapping(path = "getSubjectPeriods/{subjectId}")
    @ApiOperation(value = "获取指定主体的预算期间策略", notes = "获取指定主体的预算期间策略")
    ResultData<List<SubjectPeriodDto>> getSubjectPeriods(@PathVariable("subjectId") String subjectId);
}