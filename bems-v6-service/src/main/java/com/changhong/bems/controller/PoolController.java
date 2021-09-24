package com.changhong.bems.controller;

import com.changhong.bems.api.PoolApi;
import com.changhong.bems.dto.BudgetUseResult;
import com.changhong.bems.dto.LogRecordDto;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.entity.LogRecordView;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.util.DateUtils;
import com.changhong.sei.util.IdGenerator;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预算池(Pool)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@RestController
@Api(value = "PoolApi", tags = "预算池服务")
@RequestMapping(path = PoolApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class PoolController implements PoolApi {

    /**
     * 预算池服务对象
     */
    @Autowired
    private PoolService service;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCode 预算池代码
     * @return 预算池
     */
    @Override
    public ResultData<BudgetUseResult> getPoolByCode(String poolCode) {
        PoolAttributeView attribute = service.findPoolAttribute(poolCode);
        if (Objects.nonNull(attribute)) {
            BudgetUseResult result = new BudgetUseResult(attribute.getCode(), attribute.getTotalAmount(), attribute.getUsedAmount(),attribute.getBalance(), new BigDecimal("0"));
            StringJoiner display = new StringJoiner("|")
                    // 期间
                    .add(attribute.getPeriodName())
                    // 科目
                    .add(attribute.getItemName());
            // 组织
            if (StringUtils.isNotBlank(attribute.getOrgName())) {
                display.add(attribute.getOrgName());
            }
            // 项目
            if (StringUtils.isNotBlank(attribute.getProjectName())) {
                display.add(attribute.getProjectName());
            }
            // UDF1
            if (StringUtils.isNotBlank(attribute.getUdf1Name())) {
                display.add(attribute.getUdf1Name());
            }
            // UDF2
            if (StringUtils.isNotBlank(attribute.getUdf2Name())) {
                display.add(attribute.getUdf2Name());
            }
            // UDF3
            if (StringUtils.isNotBlank(attribute.getUdf3Name())) {
                display.add(attribute.getUdf3Name());
            }
            // UDF4
            if (StringUtils.isNotBlank(attribute.getUdf4Name())) {
                display.add(attribute.getUdf4Name());
            }
            // UDF5
            if (StringUtils.isNotBlank(attribute.getUdf5Name())) {
                display.add(attribute.getUdf5Name());
            }
            result.setDisplay(display.toString());
            return ResultData.success();
        } else {
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<PoolAttributeDto>> findByPage(Search search) {
        PageResult<PoolAttributeView> result = service.findPoolByPage(search);
        PageResult<PoolAttributeDto> pageResult = new PageResult<>(result);
        List<PoolAttributeDto> list;
        List<PoolAttributeView> poolAttributes = result.getRows();
        if (CollectionUtils.isNotEmpty(poolAttributes)) {
            list = poolAttributes.stream().map(p -> modelMapper.map(p, PoolAttributeDto.class)).collect(Collectors.toList());
        } else {
            list = new ArrayList<>();
        }
        pageResult.setRows(list);
        return ResultData.success(pageResult);
    }

    /**
     * 通过Id获取一个预算池
     *
     * @param id 预算池Id
     * @return 预算池
     */
    @Override
    public ResultData<PoolAttributeDto> getPool(String id) {
        PoolAttributeView attribute = service.findPoolAttribute(id);
        if (Objects.nonNull(attribute)) {
            return ResultData.success(modelMapper.map(attribute, PoolAttributeDto.class));
        } else {
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    /**
     * 通过Id启用预算池
     *
     * @param ids 预算池Id集合
     * @return 启用结果
     */
    @Override
    public ResultData<Void> enable(Set<String> ids) {
        return service.updateActiveStatus(ids, Boolean.TRUE);
    }

    /**
     * 通过Id禁用预算池
     *
     * @param ids 预算池Id集合
     * @return 启用结果
     */
    @Override
    public ResultData<Void> disable(Set<String> ids) {
        return service.updateActiveStatus(ids, Boolean.FALSE);
    }

    /**
     * 滚动预算池
     *
     * @param poolId 预算池id
     * @return 滚动结果
     */
    @Override
    public ResultData<Void> trundlePool(String poolId) {
        String bizId = IdGenerator.uuid2();
        String bizCode = DateUtils.formatDate(new Date(), DateUtils.FULL_SEQ_FORMAT);
        return service.trundlePool(bizId, bizCode, poolId);
    }

    /**
     * 分页查询预算执行日志
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<LogRecordDto>> findRecordByPage(Search search) {
        PageResult<LogRecordView> pageResult = service.findRecordByPage(search);
        PageResult<LogRecordDto> result = new PageResult<>(pageResult);
        List<LogRecordView> records = pageResult.getRows();
        if (CollectionUtils.isNotEmpty(records)) {
            result.setRows(records.stream().map(r -> modelMapper.map(r, LogRecordDto.class)).collect(Collectors.toList()));
        }
        return ResultData.success(result);
    }
}