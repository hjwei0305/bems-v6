package com.changhong.bems.api;

import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.dto.OrderDetailDto;
import com.changhong.bems.dto.OrderDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.flow.FlowInvokeParams;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 预算申请单(Order)API
 *
 * @author sei
 * @since 2021-04-25 15:13:58
 */
@Valid
@FeignClient(name = "bems-v6", path = OrderApi.PATH)
public interface OrderApi extends BaseEntityApi<OrderDto> {
    String PATH = "order";

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    @GetMapping(path = "findOrgTree")
    @ApiOperation(value = "获取组织机构树(不包含冻结)", notes = "获取组织机构树(不包含冻结)")
    ResultData<List<OrganizationDto>> findOrgTree();

    /**
     * 分页查询下达注入订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findInjectionByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询下达注入订单", notes = "分页查询下达注入订单")
    ResultData<PageResult<OrderDto>> findInjectionByPage(@RequestBody Search search);

    /**
     * 分页查询下达调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findAdjustmentByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询下达调整订单", notes = "分页查询下达调整订单")
    ResultData<PageResult<OrderDto>> findAdjustmentByPage(@RequestBody Search search);

    /**
     * 分页查询分解调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findSplitByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询分解调整订单", notes = "分页查询分解调整订单")
    ResultData<PageResult<OrderDto>> findSplitByPage(@RequestBody Search search);

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @PostMapping(path = "getOrderItems/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过单据Id获取单据行项", notes = "通过单据Id分页获取单据行项")
    ResultData<PageResult<OrderDetailDto>> getOrderItems(@PathVariable("orderId") String orderId, @RequestBody Search search);

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @PostMapping(path = "clearOrderItems")
    @ApiOperation(value = "通过单据Id清空单据行项", notes = "通过单据Id清空单据行项")
    ResultData<Void> clearOrderItems(@RequestParam("orderId") String orderId);

    /**
     * 通过单据行项id删除行项
     *
     * @param detailIds 单据Id
     * @return 业务实体
     */
    @DeleteMapping(path = "removeOrderItems", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过单据行项id删除行项", notes = "通过单据行项id删除行项")
    ResultData<Void> removeOrderItems(@RequestBody String[] detailIds);

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @GetMapping(path = "checkDimension")
    @ApiOperation(value = "检查预算主体和类型是否修改", notes = "通过单据Id检查预算主体和类型是否被修改")
    ResultData<String> checkDimension(@RequestParam("orderId") String orderId,
                                      @RequestParam("subjectId") String subjectId,
                                      @RequestParam("categoryId") String categoryId);

    /**
     * 添加预算申请单行项明细
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @PostMapping(path = "addOrderDetails", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "添加预算申请单行项明细", notes = "批量添加一个预算申请单行项明细")
    ResultData<String> addOrderDetails(@RequestBody @Valid AddOrderDetail order);

    /**
     * 更新预算申请单行项金额
     *
     * @param detailId 申请单行项id
     * @param amount   金额
     * @return 返回订单头id
     */
    @PostMapping(path = "updateDetailAmount")
    @ApiOperation(value = "更新预算申请单行项金额", notes = "检查并更新预算申请单行项金额")
    ResultData<OrderDetailDto> updateDetailAmount(@RequestParam("detailId") String detailId, @RequestParam("amount") double amount);

    /**
     * 保存预算申请单
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @PostMapping(path = "saveOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存预算申请单", notes = "保存一个预算申请单")
    ResultData<OrderDto> saveOrder(@RequestBody @Valid OrderDto order);

    /**
     * 预算申请单生效
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @PostMapping(path = "effectiveOrder")
    @ApiOperation(value = "预算申请单生效", notes = "预算申请单直接生效")
    ResultData<Void> effectiveOrder(@RequestParam("orderId") String orderId);

    /**
     * 检查是否存在注入类型预制单
     *
     * @return 返回检查结果
     */
    @GetMapping(path = "checkInjectPrefab")
    @ApiOperation(value = "检查是否存在注入类型预制单", notes = "检查是否存在注入类型预制单")
    ResultData<OrderDto> checkInjectPrefab();

    /**
     * 检查是否存在调整类型预制单
     *
     * @return 返回检查结果
     */
    @GetMapping(path = "checkAdjustPrefab")
    @ApiOperation(value = "检查是否存在调整类型预制单", notes = "检查是否存在调整类型预制单")
    ResultData<OrderDto> checkAdjustPrefab();

    /**
     * 检查是否存在分解类型预制单
     *
     * @return 返回检查结果
     */
    @GetMapping(path = "checkSplitPrefab")
    @ApiOperation(value = "检查是否存在分解类型预制单", notes = "检查是否存在分解类型预制单")
    ResultData<OrderDto> checkSplitPrefab();

    ///////////////////////流程集成 start//////////////////////////////

    /**
     * 工作流获取条件属性说明
     *
     * @param businessModelCode 业务实体代码
     * @param all               是否查询全部
     * @return POJO属性说明Map
     */
    @GetMapping(path = "properties")
    @ApiOperation(value = "工作流获取条件属性说明", notes = "工作流获取条件属性说明")
    ResultData<Map<String, String>> properties(@RequestParam("businessModelCode") String businessModelCode,
                                               @RequestParam("all") Boolean all);

    /**
     * 获取条件POJO属性键值对
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @return POJO属性说明Map
     */
    @GetMapping(path = "propertiesAndValues")
    @ApiOperation(value = "通过业务实体代码,业务ID获取条件POJO属性键值对", notes = "测试")
    ResultData<Map<String, Object>> propertiesAndValues(@RequestParam("businessModelCode") String businessModelCode,
                                                        @RequestParam("id") String id,
                                                        @RequestParam(name = "all", required = false) Boolean all);

    /**
     * 获取条件POJO属性初始化值键值对
     *
     * @param businessModelCode 业务实体代码
     * @return POJO属性说明Map
     */
    @GetMapping(path = "initPropertiesAndValues")
    @ApiOperation(value = "通过业务实体代码获取条件POJO属性初始化值键值对", notes = "测试")
    ResultData<Map<String, Object>> initPropertiesAndValues(@RequestParam("businessModelCode") String businessModelCode);

    /**
     * 重置单据状态
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @param status            状态
     * @return 返回结果
     */
    @PostMapping(path = "resetState")
    @ApiOperation(value = "通过业务实体代码及单据ID重置业务单据流程状态", notes = "测试")
    ResultData<Boolean> resetState(@RequestParam("businessModelCode") String businessModelCode,
                                   @RequestParam("id") String id,
                                   @RequestParam("status") String status);

    /**
     * 预算申请单提交流程占用预算事件
     *
     * @param flowInvokeParams 服务、事件输入参数VO
     * @return 操作结果
     */
    @PostMapping(path = "submitProcessEvent", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "预算申请单提交流程占用预算事件", notes = "预算申请单提交流程占用预算事件")
    ResultData<Boolean> submitProcessEvent(@RequestBody FlowInvokeParams flowInvokeParams);


    /**
     * 移动端页面属性
     */
    @GetMapping("formPropertiesAndValues")
    @ApiOperation(value = "移动端流程接口", notes = "移动端流程接口")
    ResultData<Map<String, Object>> formPropertiesAndValues(@RequestParam("businessModelCode") String businessModelCode,
                                                            @RequestParam("id") String id);
    ///////////////////////流程集成 end//////////////////////////////
}