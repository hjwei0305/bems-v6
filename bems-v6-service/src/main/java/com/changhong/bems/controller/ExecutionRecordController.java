package com.changhong.bems.controller;

import com.changhong.bems.api.ExecutionRecordApi;
import com.changhong.bems.dto.ExecutionRecordDto;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.service.ExecutionRecordService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算执行记录(ExecutionRecord)控制类
 *
 * @author sei
 * @since 2021-04-25 15:10:04
 */
@RestController
@Api(value = "ExecutionRecordApi", tags = "预算执行记录服务")
@RequestMapping(path = ExecutionRecordApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ExecutionRecordController extends BaseEntityController<ExecutionRecord, ExecutionRecordDto> implements ExecutionRecordApi {
    /**
     * 预算执行记录服务对象
     */
    @Autowired
    private ExecutionRecordService service;

    @Override
    public BaseEntityService<ExecutionRecord> getService() {
        return service;
    }

}