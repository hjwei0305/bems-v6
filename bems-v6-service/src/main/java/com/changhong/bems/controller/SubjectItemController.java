package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectItemApi;
import com.changhong.bems.dto.StrategyItemDto;
import com.changhong.bems.dto.SubjectItemSearch;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.service.StrategyItemService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算科目(Item)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "SubjectItemApi", tags = "预算主体科目服务")
@RequestMapping(path = SubjectItemApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectItemController implements SubjectItemApi {
    /**
     * 预算科目服务对象
     */
    @Autowired
    private StrategyItemService service;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 按主体获取预算科目执行策略(预算策略菜单功能使用)
     *
     * @param search search
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<StrategyItemDto>> findPageBySubject(SubjectItemSearch search) {
        PageResult<StrategyItem> pageResult = service.findPageByCorp(search.getSubjectId(), search);
        PageResult<StrategyItemDto> result = new PageResult<>(pageResult);
        if (pageResult.getRecords() > 0) {
            List<StrategyItemDto> list = pageResult.getRows()
                    .stream().map(row -> modelMapper.map(row, StrategyItemDto.class)).collect(Collectors.toList());

            result.setRows(list);
        }
        return ResultData.success(result);
    }

    /**
     * 设置预算科目为主体私有
     *
     * @return 设置结果
     */
    @Override
    public ResultData<Void> turnPrivate(String subjectId, String itemCode, boolean isPrivate) {
        return service.turnPrivate(subjectId, itemCode, isPrivate);
    }

    /**
     * 配置预算科目执行策略
     *
     * @return 配置结果
     */
    @Override
    public ResultData<Void> setStrategy(String subjectId, String itemCode, String strategyId) {
        return service.setStrategy(subjectId, itemCode, strategyId);
    }
}