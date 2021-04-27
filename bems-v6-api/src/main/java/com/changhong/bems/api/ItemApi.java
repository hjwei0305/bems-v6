package com.changhong.bems.api;

import com.changhong.bems.dto.ItemDto;
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
@FeignClient(name = "bems-v6", path = ItemApi.PATH)
public interface ItemApi extends BaseEntityApi<ItemDto> {
    String PATH = "item";

    /**
     * 查询通用预算科目
     *
     * @return 查询结果
     */
    @GetMapping(path = "findByGeneral")
    @ApiOperation(value = "查询通用预算科目", notes = "查询通用预算科目")
    ResultData<List<ItemDto>> findByGeneral();

    /**
     * 根据预算主体查询私有预算科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "根据预算主体查询私有预算科目", notes = "根据预算主体查询私有预算科目")
    ResultData<List<ItemDto>> findBySubject(@RequestParam("subjectId") String subjectId);

    /**
     * 引用通用预算科目
     *
     * @param subjectId 预算主体id
     * @param ids       通用预算类型ids
     * @return 操作结果
     */
    @PostMapping(path = "reference/{subjectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "引用通用预算科目", notes = "预算主体引用通用预算科目")
    ResultData<Void> reference(@PathVariable("subjectId") String subjectId, @RequestBody List<String> ids);

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