package com.changhong.bems.api;

import com.changhong.bems.dto.ItemDto;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.sei.core.api.BaseEntityApi;
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
public interface SubjectItemApi extends BaseEntityApi<SubjectItemDto> {
    String PATH = "subjectItem";

    /**
     * 根据预算主体查询私有预算科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "根据预算主体查询私有预算科目", notes = "根据预算主体查询私有预算科目")
    ResultData<List<SubjectItemDto>> findBySubject(@RequestParam("subjectId") String subjectId);

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

}