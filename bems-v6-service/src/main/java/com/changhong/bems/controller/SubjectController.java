package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectApi;
import com.changhong.bems.dto.SubjectDto;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.service.SubjectService;
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

/**
 * 预算主体(Subject)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@RestController
@Api(value = "SubjectApi", tags = "预算主体服务")
@RequestMapping(path = SubjectApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController extends BaseEntityController<Subject, SubjectDto> implements SubjectApi {
    /**
     * 预算主体服务对象
     */
    @Autowired
    private SubjectService service;

    @Override
    public BaseEntityService<Subject> getService() {
        return service;
    }

    /**
     * 分页查询业务实体
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<SubjectDto>> findByPage(Search search) {
        return convertToDtoPageResult(service.findByPage(search));
    }
}