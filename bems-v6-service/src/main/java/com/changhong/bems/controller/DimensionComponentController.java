package com.changhong.bems.controller;

import com.changhong.bems.api.DimensionApi;
import com.changhong.bems.api.DimensionComponentApi;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.bems.entity.Period;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.service.PeriodService;
import com.changhong.bems.service.SubjectItemService;
import com.changhong.bems.service.SubjectService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.util.EnumUtils;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算维度(Dimension)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@RestController
@Api(value = "DimensionComponentApi", tags = "预算维度UI组件服务")
@RequestMapping(path = DimensionApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class DimensionComponentController implements DimensionComponentApi {
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private PeriodService periodService;
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private SubjectItemService subjectItemService;
    /**
     * 预算主体服务对象
     */
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ModelMapper modelMapper;


    /**
     * 获取指定预算主体的科目(维度组件专用)
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public ResultData<List<SubjectItemDto>> getBudgetItems(String subjectId) {
        List<SubjectItem> subjectItems = subjectItemService.findBySubjectUnfrozen(subjectId);
        return ResultData.success(subjectItems.stream().map(s -> modelMapper.map(s, SubjectItemDto.class)).collect(Collectors.toList()));
    }

    /**
     * 按预算主体和期间类型获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    @Override
    public ResultData<List<PeriodDto>> getPeriods(String subjectId, String type) {
        List<Period> periods = periodService.findBySubjectUnclosed(subjectId, EnumUtils.getEnum(PeriodType.class, type));
        return ResultData.success(periods.stream().map(s -> modelMapper.map(s, PeriodDto.class)).collect(Collectors.toList()));
    }

    /**
     * 按预算主体获取组织机构
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    @Override
    public ResultData<OrganizationDto> getOrgTree(String subjectId) {
        return subjectService.getOrgTree(subjectId);
    }
}