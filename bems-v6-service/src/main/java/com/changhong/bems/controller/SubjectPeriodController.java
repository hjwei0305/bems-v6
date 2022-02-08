package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectPeriodApi;
import com.changhong.bems.dto.AssigneItemRequest;
import com.changhong.bems.dto.SubjectPeriodDto;
import com.changhong.bems.entity.SubjectPeriod;
import com.changhong.bems.service.SubjectPeriodService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 预算期间策略(SubjectPeriod)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "SubjectPeriodApi", tags = "预算期间策略服务")
@RequestMapping(path = SubjectPeriodApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectPeriodController extends BaseEntityController<SubjectPeriod, SubjectPeriodDto> implements SubjectPeriodApi {
    /**
     * 预算期间策略服务对象
     */
    @Autowired
    private SubjectPeriodService service;

    @Override
    public BaseEntityService<SubjectPeriod> getService() {
        return service;
    }

    /**
     * 冻结预算期间策略
     *
     * @param ids 预算期间策略id
     * @return 操作结果
     */
    @Override
    public ResultData<Void> frozen(List<String> ids) {
        return service.frozen(ids, Boolean.TRUE);
    }

    /**
     * 解冻预算期间策略
     *
     * @param ids 预算期间策略id
     * @return 操作结果
     */
    @Override
    public ResultData<Void> unfrozen(List<String> ids) {
        return service.frozen(ids, Boolean.FALSE);
    }

    /**
     * 获取指定主体的预算期间策略
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public ResultData<List<SubjectPeriodDto>> getSubjectPeriods(String subjectId) {
        return ResultData.success(convertToDtos(service.findBySubject(subjectId)));
    }
}