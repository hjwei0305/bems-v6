package com.changhong.bems.controller;

import com.changhong.bems.api.SubjectApi;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.SubjectOrganization;
import com.changhong.bems.service.SubjectService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.auth.AuthEntityData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BaseEntityService<Subject> getService() {
        return service;
    }

    /**
     * 通过业务实体Id清单获取数据权限实体清单
     *
     * @param ids 业务实体Id清单
     * @return 数据权限实体清单
     */
    @Override
    public ResultData<List<AuthEntityData>> getAuthEntityDataByIds(List<String> ids) {
        return ResultData.success(service.getAuthEntityDataByIds(ids));
    }

    /**
     * 获取当前用户有权限的业务实体清单
     *
     * @param featureCode 功能项代码
     * @return 有权限的业务实体清单
     */
    @Override
    public ResultData<List<SubjectDto>> getUserAuthorizedEntities(String featureCode) {
        return ResultData.success(convertToDtos(service.getUserAuthorizedEntities(featureCode)));
    }

    /**
     * 获取所有数据权限实体清单
     *
     * @return 数据权限实体清单
     */
    @Override
    public ResultData<List<AuthEntityData>> findAllAuthEntityData() {
        return ResultData.success(service.findAllAuthEntityData());
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

    /**
     * 启用一个预算主体
     *
     * @param id id
     * @return 启用结果
     */
    @Override
    public ResultData<Void> enable(String id) {
        return service.updateFrozen(id, Boolean.FALSE);
    }

    /**
     * 禁用一个预算主体
     *
     * @param id id
     * @return 禁用结果
     */
    @Override
    public ResultData<Void> disable(String id) {
        return service.updateFrozen(id, Boolean.TRUE);
    }

    /**
     * 获取币种数据
     *
     * @return 查询结果
     */
    @Override
    public ResultData<List<CurrencyDto>> findCurrencies() {
        return service.findCurrencies();
    }

    /**
     * 获取当前用户有权限的公司
     *
     * @return 当前用户有权限的公司
     */
    @Override
    public ResultData<List<CorporationDto>> findUserAuthorizedCorporations() {
        return service.findUserAuthorizedCorporations();
    }

    /**
     * 按公司代码获取组织机构树(不包含冻结)
     *
     * @param corpCode 公司代码
     * @return 组织机构树清单
     */
    @Override
    public ResultData<OrganizationDto> findOrgTreeByCorpCode(String corpCode) {
        return service.findOrgTree(corpCode);
    }

    /**
     * 按组织级主体id获取分配的组织机构
     *
     * @param id 组织级主体id
     * @return 分配的组织机构清单
     */
    @Override
    public ResultData<List<SubjectOrganizationDto>> getSubjectOrganizations(String id) {
        List<SubjectOrganizationDto> list;
        List<SubjectOrganization> organizations = service.getSubjectOrganizations(id);
        if (CollectionUtils.isNotEmpty(organizations)) {
            list = organizations.stream().map(o -> modelMapper.map(o, SubjectOrganizationDto.class)).collect(Collectors.toList());
        } else {
            list = Collections.emptyList();
        }
        return ResultData.success(list);
    }
}