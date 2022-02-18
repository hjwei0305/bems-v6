package com.changhong.bems.api;

import com.changhong.bems.dto.*;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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
@FeignClient(name = "bems-v6", path = ItemApi.PATH)
public interface ItemApi extends BaseEntityApi<BudgetItemDto> {
    String PATH = "item";

    /**
     * 分页查询通用预算科目
     *
     * @return 查询结果
     */
    @PostMapping(path = "findByGeneral", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询通用预算科目", notes = "分页查询通用预算科目")
    ResultData<PageResult<BudgetItemDto>> findByGeneral(@RequestBody BudgetItemSearch search);

    /**
     * 分页查询公司预算科目
     *
     * @return 查询结果
     */
    @PostMapping(path = "findByCorp", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询公司预算科目", notes = "分页查询公司预算科目")
    ResultData<PageResult<BudgetItemDto>> findByCorp(@RequestBody BudgetItemSearch search);

    /**
     * 分页查询主体预算科目
     *
     * @return 查询结果
     */
    @PostMapping(path = "findBySubject", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询主体预算科目", notes = "分页查询主体预算科目")
    ResultData<PageResult<BudgetItemDto>> findBySubject(@RequestBody SubjectItemSearch search);

    /**
     * 分页获取预算科目(外部系统集成专用)
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "getBudgetItems", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页获取预算科目", notes = "分页获取预算科目(外部系统集成专用)")
    ResultData<PageResult<BudgetItemDto>> getBudgetItems(@RequestBody Search search);

    /**
     * 禁用预算科目
     *
     * @param request 预算科目操作
     * @return 操作结果
     */
    @PostMapping(path = "disabled", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "禁用预算科目", notes = "禁用预算科目")
    ResultData<Void> disabled(@RequestBody BudgetItemDisableRequest request);

    /**
     * 导入预算科目
     *
     * @param request 预算科目操作
     * @return 操作结果
     */
    @PostMapping(path = "import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入预算科目", notes = "导入预算科目")
    ResultData<Void> importItem(@RequestBody List<BudgetItemDto> request);

    /**
     * 导出预算科目
     *
     * @return 操作结果
     */
    @PostMapping(path = "export")
    @ApiOperation(value = "导出预算科目", notes = "导出预算科目")
    ResultData<List<BudgetItemExport>> exportItem();


}