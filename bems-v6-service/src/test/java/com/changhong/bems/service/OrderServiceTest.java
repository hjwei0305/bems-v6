package com.changhong.bems.service;

import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderDimension;
import com.changhong.bems.dto.OrderDto;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.core.util.ValidUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 16:49
 */
class OrderServiceTest extends BaseUnit5Test {

    @Autowired
    private OrderService service;
    @Autowired
    private OrderDetailService orderDetailService;

    @Test
    void checkAndGetDimension() {
        String orderId = "2A159A8B-B301-11EB-B481-0242C0A84427";
        String subjectId = "5B03DED0-A3F4-11EB-A297-0242C0A8442D";
        String categoryId = "D4B5BC30-B25B-11EB-86DD-0242C0A8442C";
        ResultData<Void> resultData = service.checkAndGetDimension(orderId, subjectId, categoryId);
        System.out.println(resultData);
    }

    @Test
    void addOrderDetails() {
        String json = "{\"subjectId\":\"AC695DAB-532E-11EC-A5BE-0242C0A84425\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"categoryId\":\"18FBD0A7-5BBF-11EC-BD17-0242C0A84424\",\"orderCategory\":\"INJECTION\",\"periodType\":\"MONTHLY\",\"subjectName\":\"共享服务事业部\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"费用报销\",\"period\":[{\"text\":\"2021年1月\",\"value\":\"BC8030A2-5366-11EC-AE16-0242C0A84425\"}],\"item\":[{\"text\":\"费用-差旅费\",\"value\":\"00001\"}],\"org\":[{\"text\":\"共享服务事业部\",\"value\":\"94F403E2-D0E1-11EA-93C3-0242C0A8460D\"}],\"docIds\":[]}";
        AddOrderDetail detail = JsonUtils.fromJson(json, AddOrderDetail.class);
        ResultData<String> resultData = service.addOrderDetails(detail);
        System.out.println(resultData);
    }



    @Test
    void batchAddOrderItems() {
        String orderId = "1111";
        String categoryId = "9C40DADB-A65C-11EB-8A8F-0242C0A84427";
        AddOrderDetail detail = new AddOrderDetail();
        detail.setId(orderId);
        detail.setCategoryId(categoryId);
        detail.setOrderCategory(OrderCategory.INJECTION);

        Set<OrderDimension> set = new HashSet<>();
        set.add(new OrderDimension("1", "1"));
        set.add(new OrderDimension("2", "2"));
        detail.setPeriod(set);

        set = new HashSet<>();
        set.add(new OrderDimension("A", "A"));
        set.add(new OrderDimension("B", "B"));
        set.add(new OrderDimension("C", "C"));
        set.add(new OrderDimension("D", "D"));
        detail.setItem(set);

        set = new HashSet<>();
//        set.add(new OrderDimension("a", "a"));
//        set.add(new OrderDimension("b", "b"));
        detail.setOrg(set);
        service.addOrderDetails(detail);
    }

    @Test
    void saveOrder() {
        String json = "{\"id\":\"CCCF62C3-B26B-11EB-971A-6E883C5EFC87\",\"code\":\"0000000180\",\"subjectId\":\"5B03DED0-A3F4-11EB-A297-0242C0A8442D\",\"subjectName\":\"四川爱联科技有限公司\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"categoryId\":\"A9DA0DB5-B25B-11EB-89CD-0242C0A84429\",\"categoryName\":\"年度预算\",\"periodType\":\"ANNUAL\",\"orderCategory\":\"INJECTION\",\"applyAmount\":null,\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"managerOrgId\":null,\"managerOrgCode\":null,\"managerOrgName\":null,\"remark\":\"qqq\",\"status\":\"DRAFT\",\"creatorAccount\":\"admin\",\"creatorName\":\"系统管理员\",\"createdDate\":\"2021-05-11 23:16:03\"}";
        Order order = JsonUtils.fromJson(json, Order.class);
        ResultData<Order> resultData = service.saveOrder(order);
        System.out.println(resultData);
    }

    @Test
    void effectiveOrder() {
        StopWatch stopWatch = StopWatch.createStarted();
        String orderId = "346F7891-C445-11EB-B3C8-0242C0A8442C";
//        Order order = service.findOne(orderId);
//        List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());

        ResultData<Order> resultData = service.effective(orderId);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }

    @Test
    void confirmUseBudget() {
        StopWatch stopWatch = StopWatch.createStarted();

        String orderId = "F5B05C90-CE7A-11EB-997A-0242C0A8442C";
        ResultData<Order> resultData = service.confirm(orderId);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }

    @Test
    void effectiveUseBudget() {
        StopWatch stopWatch = StopWatch.createStarted();

        String orderId = "16837168-CD9B-11EB-A68D-0242C0A84429";
        ResultData<Order> resultData = service.effective(orderId);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }

    @Test
    void testValid() {
        OrderDto order = new OrderDto();
        order.setCode("12345678901");

        ResultData<Void> resultData = ValidUtils.validate(order);
        System.out.println(resultData);
    }
}