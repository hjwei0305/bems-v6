package com.changhong.bems.controller;

import com.changhong.bems.api.PoolApi;
import com.changhong.bems.dto.PoolLogDto;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolQuickQueryParam;
import com.changhong.bems.entity.PoolLog;
import com.changhong.bems.entity.PoolLogView;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.util.DateUtils;
import com.changhong.sei.util.IdGenerator;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Set;
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
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<PoolAttributeDto>> findByPage(PoolQuickQueryParam search) {
        return ResultData.success(service.findPoolByPage(search));
    }

    /**
     * 通过Id获取一个预算池
     *
     * @param id 预算池Id
     * @return 预算池
     */
    @Override
    public ResultData<PoolAttributeDto> getPool(String id) {
        return service.findPoolAttribute(id);
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
    public ResultData<PageResult<PoolLogDto>> findRecordByPage(Search search) {
        PageResult<PoolLog> pageResult = service.findRecordByPage(search);
        PageResult<PoolLogDto> result = new PageResult<>(pageResult);
        List<PoolLog> records = pageResult.getRows();
        if (CollectionUtils.isNotEmpty(records)) {
            result.setRows(records.stream().map(r -> modelMapper.map(r, PoolLogDto.class)).collect(Collectors.toList()));
        }
        return ResultData.success(result);
    }
}