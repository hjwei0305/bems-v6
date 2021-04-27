package com.changhong.bems.api;

import com.changhong.bems.dto.*;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
public interface SubjectItemApi extends BaseEntityApi<SubjectItemDto>, FindByPageApi<SubjectItemDto> {
    String PATH = "subjectItem";

    /**
     * 冻结预算科目
     *
     * @param ids 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "frozen")
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> frozen(@RequestBody List<String> ids);

    /**
     * 解冻预算科目
     *
     * @param ids 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "unfrozen")
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> unfrozen(@RequestBody List<String> ids);

    /**
     * 获取未分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @GetMapping(path = "getUnassigned")
    @ApiOperation(value = "获取未分配的预算科目", notes = "获取未分配的预算科目")
    ResultData<List<ItemDto>> getUnassigned(@RequestParam("subjectId") String subjectId);

    /**
     * 获取已分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @GetMapping(path = "getAssigned")
    @ApiOperation(value = "获取已分配的预算科目", notes = "获取已分配的预算科目")
    ResultData<List<ItemDto>> getAssigned(@RequestParam("subjectId") String subjectId);

    /**
     * 为指定预算主体分配预算科目
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping(path = "assigne")
    @ApiOperation(value = "为指定预算主体分配预算科目", notes = "为指定预算主体分配预算科目")
    ResultData<Void> assigne(@RequestBody @Valid AssigneItemRequest request);

    /**
     * 解除预算主体与科目分配关系
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping(path = "unassigne")
    @ApiOperation(value = "解除预算主体与科目分配关系", notes = "解除预算主体与科目分配关系")
    ResultData<Void> unassigne(@RequestBody @Valid AssigneItemRequest request);

}