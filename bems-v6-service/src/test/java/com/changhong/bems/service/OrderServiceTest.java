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
        String json = "{\"subjectId\":\"5B03DED0-A3F4-11EB-A297-0242C0A8442D\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"managerOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"managerOrgCode\":\"10607\",\"categoryId\":\"A62B158F-A6FD-11EB-AFFE-0242C0A84427\",\"orderCategory\":\"INJECTION\",\"periodType\":\"MONTHLY\",\"subjectName\":\"四川爱联科技有限公司\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"月度预算\",\"managerOrgName\":\"四川长虹电子控股集团有限公司\",\"remark\":\"1111\",\"item\":[{\"text\":\"通讯费\",\"value\":\"1000\"},{\"text\":\"办公费\",\"value\":\"2000\"},{\"text\":\"国内差旅费\",\"value\":\"3000\"}],\"period\":[{\"text\":\"2021年1月\",\"value\":\"623F165F-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年2月\",\"value\":\"623F1660-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年3月\",\"value\":\"623F3D71-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年4月\",\"value\":\"623F3D72-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年5月\",\"value\":\"623F3D73-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年6月\",\"value\":\"623F3D74-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年7月\",\"value\":\"623F3D75-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年8月\",\"value\":\"623F6486-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年9月\",\"value\":\"623F6487-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年10月\",\"value\":\"623F6488-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年11月\",\"value\":\"623F6489-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年12月\",\"value\":\"623F648A-A56A-11EB-AA8E-0242C0A8442D\"}]}";
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
        ResultData<Order> resultData = service.saveOrder(order, null);
        System.out.println(resultData);
    }

    @Test
    void effectiveOrder() {
        StopWatch stopWatch = StopWatch.createStarted();
        String orderId = "346F7891-C445-11EB-B3C8-0242C0A8442C";
//        Order order = service.findOne(orderId);
//        List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());

        ResultData<Void> resultData = service.effective(orderId);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }

    @Test
    void submitProcess() {
        StopWatch stopWatch = StopWatch.createStarted();

        String orderId = "1979E1C4-AFC8-11EB-B9C2-0242C0A84427";
        String taskActDefId = "ReceiveTask_2";
        Order order = service.findOne(orderId);
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        ResultData<Void> resultData = service.submitProcess(order, details, taskActDefId);
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