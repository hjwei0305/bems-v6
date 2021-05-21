package com.changhong.bems.api;

import com.changhong.bems.dto.EventDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-11 23:07
 */
@Valid
@FeignClient(name = "bems-v6", path = EventApi.PATH)
public interface EventApi extends BaseEntityApi<EventDto>, FindAllApi<EventDto> {
    String PATH = "event";

    /**
     * 按指定标签获取预算事件
     *
     * @return 预算事件清单
     */
    @GetMapping(path = "findByLabel")
    @ApiOperation(value = "按指定标签获取预算事件", notes = "按指定标签获取预算事件")
    ResultData<List<EventDto>> findByLabel(@RequestParam("label") String label);

    /**
     * 按指定业务来源系统获取预算事件
     *
     * @return 预算事件清单
     */
    @GetMapping(path = "findByBizFrom")
    @ApiOperation(value = "按指定业务来源系统获取预算事件", notes = "按指定业务来源系统获取预算事件")
    ResultData<List<EventDto>> findByBizFrom(@RequestParam("bizFrom") String bizFrom);
}
