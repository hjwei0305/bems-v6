package com.changhong.bems.controller;

import com.changhong.bems.api.EventApi;
import com.changhong.bems.dto.EventDto;
import com.changhong.bems.entity.Event;
import com.changhong.bems.service.EventService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预算事件(Event)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "EventApi", tags = "预算事件服务")
@RequestMapping(path = EventApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class EventController extends BaseEntityController<Event, EventDto> implements EventApi {
    /**
     * 预算事件服务对象
     */
    @Autowired
    private EventService service;

    @Override
    public BaseEntityService<Event> getService() {
        return service;
    }

    /**
     * 获取所有业务实体
     *
     * @return 业务实体清单
     */
    @Override
    public ResultData<List<EventDto>> findAll() {
        List<Event> eventList = service.findAll();
        if (CollectionUtils.isEmpty(eventList)) {
            ResultData<List<Event>> resultData = service.checkAndInit();
            if (resultData.failed()) {
                return ResultData.fail(resultData.getMessage());
            } else {
                eventList = resultData.getData();
            }
        }
        return ResultData.success(convertToDtos(eventList));
    }

    /**
     * 获取所有未冻结的业务实体
     *
     * @return 业务实体清单
     */
    @Override
    public ResultData<List<EventDto>> findAllUnfrozen() {
        return ResultData.success(convertToDtos(service.findAllUnfrozen()));
    }

    /**
     * 按指定标签获取预算事件
     *
     * @param label 标签
     * @return 预算事件清单
     */
    @Override
    public ResultData<List<EventDto>> findByLabel(String label) {
        List<Event> eventList = service.findByLabel(label);
        return ResultData.success(convertToDtos(eventList));
    }

    /**
     * 按指定业务来源系统获取预算事件
     *
     * @param bizFrom 业务来源系统
     * @return 预算事件清单
     */
    @Override
    public ResultData<List<EventDto>> findByBizFrom(String bizFrom) {
        List<Event> eventList = service.findByBizFrom(bizFrom);
        return ResultData.success(convertToDtos(eventList));
    }
}