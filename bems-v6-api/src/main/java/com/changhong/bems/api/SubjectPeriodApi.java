package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyPeriodDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
public interface SubjectPeriodApi {
    String PATH = "subjectPeriod";

    /**
     * 维护业务是否可使用
     *
     * @param id 预算期间策略id
     * @return 操作结果
     */
    @PostMapping(path = "use/{id}")
    @ApiOperation(value = "维护业务是否可使用", notes = "维护业务是否可使用")
    ResultData<Void> use(@PathVariable("id") String id, @RequestBody Boolean use);

    /**
     * 维护是否可结转
     *
     * @param id 预算期间策略id
     * @return 操作结果
     */
    @PostMapping(path = "roll/{id}")
    @ApiOperation(value = "维护是否可结转", notes = "维护是否可结转")
    ResultData<Void> roll(@PathVariable("id") String id, @RequestBody Boolean roll);

    /**
     * 获取指定主体的预算期间策略
     *
     * @return 子实体清单
     */
    @GetMapping(path = "getSubjectPeriods")
    @ApiOperation(value = "获取指定主体的预算期间策略", notes = "获取指定主体的预算期间策略")
    ResultData<List<StrategyPeriodDto>> getSubjectPeriods(@RequestParam("subjectId") String subjectId);
}