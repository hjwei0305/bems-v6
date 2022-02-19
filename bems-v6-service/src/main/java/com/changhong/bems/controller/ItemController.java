package com.changhong.bems.controller;

import com.changhong.bems.api.ItemApi;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.service.ItemService;
import com.changhong.bems.service.SubjectService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预算科目(Item)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@RestController
@Api(value = "ItemApi", tags = "预算科目服务")
@RequestMapping(path = ItemApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemController extends BaseEntityController<Item, BudgetItemDto> implements ItemApi {
    /**
     * 预算科目服务对象
     */
    @Autowired
    private ItemService service;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BaseEntityService<Item> getService() {
        return service;
    }

    /**
     * 分页获取预算科目(外部系统集成专用)
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<BudgetItemDto>> getBudgetItems(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        search.addFilter(new SearchFilter(Item.FROZEN, Boolean.FALSE));
        return convertToDtoPageResult(service.findByPage(search));
    }

    /**
     * 分页查询通用预算科目
     *
     * @return 查询结果
     */
    @Override
    public ResultData<PageResult<BudgetItemDto>> findByGeneral(BudgetItemSearch search) {
        Boolean disable = search.getDisabled();
        if (Objects.nonNull(disable)) {
            search.addFilter(new SearchFilter(Item.FROZEN, disable));
        }
        return convertToDtoPageResult(service.findByPage(search));
    }

    /**
     * 分页查询公司预算科目
     *
     * @return 查询结果
     */
    @Override
    public ResultData<PageResult<BudgetItemDto>> findByCorp(BudgetItemSearch search) {
        Boolean disabled = search.getDisabled();
        // List<SearchFilter> filters = search.getFilters();
        // if (CollectionUtils.isNotEmpty(filters)) {
        //     for (SearchFilter filter : filters) {
        //         if (Item.FROZEN.equals(filter.getFieldName())) {
        //             disabled = Boolean.parseBoolean("" + filter.getValue());
        //             break;
        //         }
        //     }
        // }
        return convertToDtoPageResult(service.findPageByCorp(search.getCorpCode(), disabled, search));
    }

    /**
     * 分页查询主体预算科目
     *
     * @return 查询结果
     */
    @Override
    public ResultData<PageResult<BudgetItemDto>> findBySubject(SubjectItemSearch search) {
        Subject subject = subjectService.getSubject(search.getSubjectId());
        if (Objects.nonNull(subject)) {
            // 可用的,未禁用的科目
            return convertToDtoPageResult(service.findPageUsableByCorp(subject.getCorporationCode(), search));
        } else {
            return ResultData.fail(ContextUtil.getMessage("subject_00003", search.getSubjectId()));
        }
    }

    /**
     * 禁用预算科目
     *
     * @param request 预算科目操作请求
     * @return 操作结果
     */
    @Override
    public ResultData<Void> disabled(BudgetItemDisableRequest request) {
        return service.disabled(request.getCorpCode(), request.getIds(), request.isDisabled());
    }

    /**
     * 导入预算科目
     *
     * @param itemDtos 预算科目清单
     * @return 操作结果
     */
    @Override
    public ResultData<Void> importItem(List<BudgetItemDto> itemDtos) {
        return service.importItem(convertToEntities(itemDtos));
    }

    /**
     * 导出预算科目
     *
     * @return 操作结果
     */
    @Override
    public ResultData<List<BudgetItemExport>> exportItem() {
        List<BudgetItemExport> list;
        List<Item> itemList = service.findAll();
        if (CollectionUtils.isNotEmpty(itemList)) {
            list = itemList.stream().map(item -> modelMapper.map(item, BudgetItemExport.class))
                    .sorted(Comparator.comparing(BudgetItemExport::getCode)).collect(Collectors.toList());
        } else {
            list = new ArrayList<>();
        }
        return ResultData.success(list);
    }
}