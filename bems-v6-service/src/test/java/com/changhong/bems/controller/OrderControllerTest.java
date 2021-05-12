package com.changhong.bems.controller;

import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-11 22:00
 */
class OrderControllerTest extends BaseUnit5Test {
    @Autowired
    private OrderController controller;

    @Test
    void findOrgTree() {
        ResultData<List<OrganizationDto>> resultData = controller.findOrgTree();
        System.out.println(resultData);
    }

    @Test
    void findInjectionByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.INJECTION));
        ResultData<PageResult<OrderDto>> resultData = controller.findInjectionByPage(search);
        System.out.println(resultData);
    }

    @Test
    void findAdjustmentByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.ADJUSTMENT));
        ResultData<PageResult<OrderDto>> resultData = controller.findAdjustmentByPage(search);
        System.out.println(resultData);
    }

    @Test
    void findSplitByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.SPLIT));
        ResultData<PageResult<OrderDto>> resultData = controller.findSplitByPage(search);
        System.out.println(resultData);
    }

    @Test
    void getOrderItems() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        Search search = Search.createSearch();
        controller.getOrderItems(orderId, search);
    }

    @Test
    void clearOrderItems() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        ResultData<Void> resultData = controller.clearOrderItems(orderId);
        System.out.println(resultData);
    }

    @Test
    void removeOrderItems() {
        String[] detailIds = new String[]{""};
        ResultData<Void> resultData = controller.removeOrderItems(detailIds);
        System.out.println(resultData);
    }

    @Test
    void checkDimension() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        String subjectId = "";
        String categoryId = "";
        ResultData<OrderDto> resultData = controller.checkDimension(orderId, subjectId, categoryId);
        System.out.println(resultData);
    }

    @Test
    void addOrderDetails() {
        StopWatch stopWatch = StopWatch.createStarted();
        String json = "{\"subjectId\":\"5B03DED0-A3F4-11EB-A297-0242C0A8442D\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"categoryId\":\"A9DA0DB5-B25B-11EB-89CD-0242C0A84429\",\"orderCategory\":\"INJECTION\",\"periodType\":\"ANNUAL\",\"subjectName\":\"四川爱联科技有限公司\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"年度预算\",\"remark\":\"qqq\",\"period\":[{\"text\":\"2021年度\",\"value\":\"64358F80-A988-11EB-B18E-0242C0A84427\"}],\"item\":[{\"text\":\"办公费\",\"value\":\"2000\"},{\"text\":\"国内差旅费\",\"value\":\"3000\"}]}";
        AddOrderDetail detail = JsonUtils.fromJson(json, AddOrderDetail.class);
        ResultData<String> resultData = controller.addOrderDetails(detail);
        System.out.println(resultData);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
    }

    @Test
    void updateDetailAmount() {
        String detailId = "3C08E322-B25D-11EB-AB8A-0242C0A84429";
        double amount = 100;
        ResultData<OrderDetailDto> resultData = controller.updateDetailAmount(detailId, amount);
        System.out.println(resultData);
    }

    @Test
    void saveOrder() {
        String json = "";
        OrderDto request = JsonUtils.fromJson(json, OrderDto.class);
        ResultData<OrderDto> resultData = controller.saveOrder(request);
        System.out.println(resultData);
    }

    @Test
    void effectiveOrder() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        ResultData<Void> resultData = controller.effectiveOrder(orderId);
        System.out.println(resultData);
    }

//    @Test
//    void submitProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        Order order = controller.findOne(orderId);
//        ResultData<Void> resultData = controller.submitProcess(orderId);
//        System.out.println(resultData);
//    }
//
//    @Test
//    void cancelProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        ResultData<Void> resultData = controller.cancelProcess(orderId);
//        System.out.println(resultData);
//    }
//
//    @Test
//    void completeProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        ResultData<Void> resultData = controller.completeProcess(orderId);
//        System.out.println(resultData);
//    }
}