package com.changhong.bems.controller;

import com.changhong.bems.api.PeriodApi;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.bems.entity.Period;
import com.changhong.bems.service.PeriodService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算期间(Period)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@RestController
@Api(value = "PeriodApi", tags = "预算期间服务")
@RequestMapping(path = PeriodApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class PeriodController extends BaseEntityController<Period, PeriodDto> implements PeriodApi {
    /**
     * 预算期间服务对象
     */
    @Autowired
    private PeriodService service;

    @Override
    public BaseEntityService<Period> getService() {
        return service;
    }

    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<PeriodDto>> findByPage(Search search) {
        return convertToDtoPageResult(service.findByPage(search));
    }
}