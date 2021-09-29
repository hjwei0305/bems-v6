package com.changhong.bems.controller;

import com.changhong.bems.api.ReportApi;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.service.CategoryService;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现功能：预算分析报表服务
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-29 08:41
 */
@RestController
@Api(value = "ReportApi", tags = "预算分析报表服务")
@RequestMapping(path = ReportApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportController implements ReportApi {


    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 通过预算主体获取其使用的维度清单
     *
     * @param subjectId 预算主体id
     * @return 使用预算结果
     */
    @Override
    public ResultData<List<DimensionDto>> getDimensionsBySubjectId(String subjectId) {
        List<Dimension> dimensions = categoryService.findDimensionBySubject(subjectId);
        List<DimensionDto> dtoList = dimensions.stream().map(d -> modelMapper.map(d, DimensionDto.class)).collect(Collectors.toList());
        return ResultData.success(dtoList);
    }
}
