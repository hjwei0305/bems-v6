package com.changhong.bems.api;

import com.changhong.bems.dto.CategoryDto;
import com.changhong.bems.dto.ItemDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
public interface ItemApi extends BaseEntityApi<ItemDto>, FindByPageApi<ItemDto> {
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
     * @param id        通用预算类型id
     * @return 操作结果
     */
    @PostMapping(path = "reference/{subjectId}/{id}")
    @ApiOperation(value = "引用通用预算科目", notes = "预算主体引用通用预算科目")
    ResultData<Void> reference(@PathVariable("subjectId") String subjectId, @PathVariable("id") String id);

    /**
     * 冻结预算科目
     *
     * @param id 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "frozen/{id}")
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> frozen(@PathVariable("id") String id);

    /**
     * 解冻预算科目
     *
     * @param id 预算科目id
     * @return 操作结果
     */
    @PostMapping(path = "unfrozen/{id}")
    @ApiOperation(value = "冻结预算科目", notes = "冻结预算科目")
    ResultData<Void> unfrozen(@PathVariable("id") String id);

}