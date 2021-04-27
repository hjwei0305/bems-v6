package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectItemApi;
import com.changhong.bems.dto.AssigneItemRequest;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.service.SubjectItemService;
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

import java.util.List;

/**
 * 预算科目(Item)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "SubjectItemApi", tags = "预算主体科目服务")
@RequestMapping(path = SubjectItemApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectItemController extends BaseEntityController<SubjectItem, SubjectItemDto> implements SubjectItemApi {
    /**
     * 预算科目服务对象
     */
    @Autowired
    private SubjectItemService service;

    @Override
    public BaseEntityService<SubjectItem> getService() {
        return service;
    }

    /**
     * 冻结预算科目
     *
     * @param ids 预算类型id
     * @return 操作结果
     */
    @Override
    public ResultData<Void> frozen(List<String> ids) {
        return service.frozen(ids, Boolean.TRUE);
    }

    /**
     * 解冻预算科目
     *
     * @param ids 预算类型id
     * @return 操作结果
     */
    @Override
    public ResultData<Void> unfrozen(List<String> ids) {
        return service.frozen(ids, Boolean.FALSE);
    }

    /**
     * 获取未分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public ResultData<PageResult<SubjectItemDto>> getUnassigned(String subjectId, Search search) {
        return convertToDtoPageResult(service.getAssigned(subjectId, search));
    }

    /**
     * 获取已分配的预算科目
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public ResultData<PageResult<SubjectItemDto>> getAssigned(String subjectId, Search search) {
        return convertToDtoPageResult(service.getAssigned(subjectId, search));
    }

    /**
     * 为指定预算主体分配预算科目
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @Override
    public ResultData<Void> assigne(AssigneItemRequest request) {
        return service.assigne(request.getSubjectId(), request.getItemCodes());
    }

    /**
     * 解除预算主体与科目分配关系
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @Override
    public ResultData<Void> unassigne(AssigneItemRequest request) {
        return service.unassigne(request.getSubjectId(), request.getItemCodes());
    }
}