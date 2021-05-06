package com.changhong.bems.service;

import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderDimension;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 01:04
 */
class OrderDetailServiceTest extends BaseUnit5Test {

    @Autowired
    private OrderDetailService service;

    @Test
    void clearOrderItems() {
        String orderId = "";
        service.clearOrderItems(orderId);
    }

    @Test
    void batchAddOrderItems() {
        String orderId = "1111";
        String categoryId = "9C40DADB-A65C-11EB-8A8F-0242C0A84427";
        AddOrderDetail detail = new AddOrderDetail();
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
        Order order = new Order();
        order.setId(orderId);
        order.setCategoryId(categoryId);
        order.setOrderCategory(OrderCategory.INJECTION);
        service.batchAddOrderItems(order, detail);

    }
}