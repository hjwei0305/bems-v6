package com.changhong.bems.controller;

import com.changhong.bems.api.PoolApi;
import com.changhong.bems.dto.BudgetPoolAmountDto;
import com.changhong.bems.dto.LogRecordDto;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.entity.LogRecordView;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.util.DateUtils;
import com.changhong.sei.util.IdGenerator;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResultData<BudgetPoolAmountDto> getPoolByCode(String poolCode) {
        PoolAttributeView attribute = service.findPoolAttributeByCode(poolCode);
        if (Objects.nonNull(attribute)) {
            BudgetPoolAmountDto result = new BudgetPoolAmountDto(attribute.getCode(), attribute.getTotalAmount(), attribute.getUsedAmount(), attribute.getBalance());
            result.setPeriod(attribute.getPeriod());
            result.setPeriodName(attribute.getPeriodName());
            result.setItem(attribute.getItem());
            result.setItemName(attribute.getItemName());
            result.setOrg(attribute.getOrg());
            result.setOrgName(attribute.getOrgName());
            result.setProject(attribute.getProject());
            result.setProjectName(attribute.getProjectName());
            result.setUdf1(attribute.getUdf1());
            result.setUdf1Name(attribute.getUdf1Name());
            result.setUdf2(attribute.getUdf2());
            result.setUdf2Name(attribute.getUdf2Name());
            result.setUdf3(attribute.getUdf3());
            result.setUdf3Name(attribute.getUdf3Name());
            result.setUdf4(attribute.getUdf4());
            result.setUdf4Name(attribute.getUdf4Name());
            result.setUdf5(attribute.getUdf5());
            result.setUdf5Name(attribute.getUdf5Name());
            return ResultData.success(result);
        } else {
            return ResultData.fail(ContextUtil.getMessage("pool_00001"));
        }
    }

    /**
     * 通过预算池代码获取一个预算池
     *
     * @param poolCodes 预算池代码
     * @return 预算池
     */
    @Override
    public ResultData<List<BudgetPoolAmountDto>> getPoolsByCode(List<String> poolCodes) {
        List<PoolAttributeView> attributes = service.findPoolAttributes(poolCodes);
        if (CollectionUtils.isNotEmpty(attributes)) {
            BudgetPoolAmountDto result;
            List<BudgetPoolAmountDto> results = new ArrayList<>();
            for(PoolAttributeView attribute : attributes) {
                result = new BudgetPoolAmountDto(attribute.getCode(), attribute.getTotalAmount(), attribute.getUsedAmount(), attribute.getBalance());
                result.setPeriod(attribute.getPeriod());
                result.setPeriodName(attribute.getPeriodName());
                result.setItem(attribute.getItem());
                result.setItemName(attribute.getItemName());
                result.setOrg(attribute.getOrg());
                result.setOrgName(attribute.getOrgName());
                result.setProject(attribute.getProject());
                result.setProjectName(attribute.getProjectName());
                result.setUdf1(attribute.getUdf1());
                result.setUdf1Name(attribute.getUdf1Name());
                result.setUdf2(attribute.getUdf2());
                result.setUdf2Name(attribute.getUdf2Name());
                result.setUdf3(attribute.getUdf3());
                result.setUdf3Name(attribute.getUdf3Name());
                result.setUdf4(attribute.getUdf4());
                result.setUdf4Name(attribute.getUdf4Name());
                result.setUdf5(attribute.getUdf5());
                result.setUdf5Name(attribute.getUdf5Name());
                results.add(result);
            }
            return ResultData.success(results);
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