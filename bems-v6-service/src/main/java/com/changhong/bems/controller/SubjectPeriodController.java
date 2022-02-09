package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectPeriodApi;
import com.changhong.bems.dto.SubjectPeriodDto;
import com.changhong.bems.entity.SubjectPeriod;
import com.changhong.bems.service.SubjectPeriodService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预算期间策略(SubjectPeriod)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "SubjectPeriodApi", tags = "预算期间策略服务")
@RequestMapping(path = SubjectPeriodApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectPeriodController implements SubjectPeriodApi {
    /**
     * 预算期间策略服务对象
     */
    @Autowired
    private SubjectPeriodService service;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 维护业务是否可使用
     *
     * @param id  预算期间策略id
     * @param use
     * @return 操作结果
     */
    @Override
    public ResultData<Void> use(String id, Boolean use) {
        SubjectPeriod subjectPeriod = service.findOne(id);
        if (Objects.isNull(subjectPeriod)) {
            return ResultData.fail(ContextUtil.getMessage("pool_00028"));
        }
        subjectPeriod.setUse(use);
        service.save(subjectPeriod);
        return ResultData.success();
    }

    /**
     * 维护是否可结转
     *
     * @param id   预算期间策略id
     * @param roll
     * @return 操作结果
     */
    @Override
    public ResultData<Void> roll(String id, Boolean roll) {
        SubjectPeriod subjectPeriod = service.findOne(id);
        if (Objects.isNull(subjectPeriod)) {
            return ResultData.fail(ContextUtil.getMessage("pool_00028"));
        }
        subjectPeriod.setRoll(roll);
        service.save(subjectPeriod);
        return ResultData.success();
    }

    /**
     * 获取指定主体的预算期间策略
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public ResultData<List<SubjectPeriodDto>> getSubjectPeriods(String subjectId) {
        List<SubjectPeriodDto> result;
        List<SubjectPeriod> subjectPeriods = service.findBySubject(subjectId);
        if (CollectionUtils.isNotEmpty(subjectPeriods)) {
            result = subjectPeriods.stream().map(p -> modelMapper.map(p, SubjectPeriodDto.class)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
        }
        return ResultData.success(result);
    }
}