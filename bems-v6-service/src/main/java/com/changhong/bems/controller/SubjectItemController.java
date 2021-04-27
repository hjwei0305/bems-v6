package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectItemApi;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.service.SubjectItemService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
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
@Api(value = "SubjectItemApi", tags = "预算科目服务")
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
     * 根据预算主体查询私有预算科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Override
    public ResultData<List<SubjectItemDto>> findBySubject(String subjectId) {
        return ResultData.success(convertToDtos(service.findBySubject(subjectId)));
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

}