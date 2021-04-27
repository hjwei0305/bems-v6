package com.changhong.bems.api;

import com.changhong.bems.dto.AssigneItemRequest;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算科目(Item)API
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectItemApi.PATH)
public interface SubjectItemApi extends BaseEntityApi<SubjectItemDto> {
    String PATH = "subjectItem";

    /**
     * 冻结预算科目
     *
     * @param ids 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "frozen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> frozen(@RequestBody List<String> ids);

    /**
     * 解冻预算科目
     *
     * @param ids 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "unfrozen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> unfrozen(@RequestBody List<String> ids);

    /**
     * 获取未分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @PostMapping(path = "getUnassigned/{subjectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取未分配的预算科目", notes = "获取未分配的预算科目")
    ResultData<PageResult<SubjectItemDto>> getUnassigned(@PathVariable("subjectId") String subjectId, @RequestBody Search search);

    /**
     * 获取已分配的预算科目
     *
     * @return 子实体清单
     */
    @PostMapping(path = "getAssigned", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取已分配的预算科目", notes = "获取已分配的预算科目")
    ResultData<PageResult<SubjectItemDto>> getAssigned(@RequestBody Search search);

    /**
     * 为指定预算主体分配预算科目
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping(path = "assigne", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "为指定预算主体分配预算科目", notes = "为指定预算主体分配预算科目")
    ResultData<Void> assigne(@RequestBody @Valid AssigneItemRequest request);

}