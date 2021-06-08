package com.changhong.bems.service;

import com.changhong.bems.dto.SplitDetailQuickQueryParam;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    void querySplitGroup() {
        SplitDetailQuickQueryParam param = new SplitDetailQuickQueryParam();
        param.setOrderId("346F7891-C445-11EB-B3C8-0242C0A8442C");
        param.setQuickSearchValue("办公");
        PageResult<OrderDetail> pageResult = service.querySplitGroup(param);
        System.out.println(pageResult);
    }
}