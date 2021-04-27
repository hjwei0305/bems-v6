package com.changhong.bems.controller;

import com.changhong.bems.api.ItemApi;
import com.changhong.bems.dto.ItemDto;
import com.changhong.bems.entity.Item;
import com.changhong.bems.service.ItemService;
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
@Api(value = "ItemApi", tags = "预算科目服务")
@RequestMapping(path = ItemApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemController extends BaseEntityController<Item, ItemDto> implements ItemApi {
    /**
     * 预算科目服务对象
     */
    @Autowired
    private ItemService service;

    @Override
    public BaseEntityService<Item> getService() {
        return service;
    }

    /**
     * 查询通用预算科目
     *
     * @return 查询结果
     */
    @Override
    public ResultData<List<ItemDto>> findByGeneral() {
        return ResultData.success(convertToDtos(service.findByGeneral()));
    }

    /**
     * 根据预算主体查询私有预算科目
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Override
    public ResultData<List<ItemDto>> findBySubject(String subjectId) {
        return ResultData.success(convertToDtos(service.findBySubject(subjectId)));
    }

    /**
     * 引用通用预算科目
     *
     * @param subjectId 预算主体id
     * @param ids       通用预算类型ids
     * @return 操作结果
     */
    @Override
    public ResultData<Void> reference(String subjectId, List<String> ids) {
        return service.reference(subjectId, ids);
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