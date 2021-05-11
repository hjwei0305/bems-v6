package com.changhong.bems.service;

import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 16:49
 */
class OrderServiceTest extends BaseUnit5Test {

    @Autowired
    private OrderService service;

    @Test
    void addOrderDetails() {
        String json = "{\"subjectId\":\"5B03DED0-A3F4-11EB-A297-0242C0A8442D\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"managerOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"managerOrgCode\":\"10607\",\"categoryId\":\"A62B158F-A6FD-11EB-AFFE-0242C0A84427\",\"orderCategory\":\"INJECTION\",\"periodType\":\"MONTHLY\",\"subjectName\":\"四川爱联科技有限公司\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"月度预算\",\"managerOrgName\":\"四川长虹电子控股集团有限公司\",\"remark\":\"1111\",\"item\":[{\"text\":\"通讯费\",\"value\":\"1000\"},{\"text\":\"办公费\",\"value\":\"2000\"},{\"text\":\"国内差旅费\",\"value\":\"3000\"}],\"period\":[{\"text\":\"2021年1月\",\"value\":\"623F165F-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年2月\",\"value\":\"623F1660-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年3月\",\"value\":\"623F3D71-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年4月\",\"value\":\"623F3D72-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年5月\",\"value\":\"623F3D73-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年6月\",\"value\":\"623F3D74-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年7月\",\"value\":\"623F3D75-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年8月\",\"value\":\"623F6486-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年9月\",\"value\":\"623F6487-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年10月\",\"value\":\"623F6488-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年11月\",\"value\":\"623F6489-A56A-11EB-AA8E-0242C0A8442D\"},{\"text\":\"2021年12月\",\"value\":\"623F648A-A56A-11EB-AA8E-0242C0A8442D\"}]}";
        AddOrderDetail detail = JsonUtils.fromJson(json, AddOrderDetail.class);
        ResultData<String> resultData = service.addOrderDetails(detail);
        System.out.println(resultData);
    }

    @Test
    void saveOrder() {

    }

    @Test
    void effectiveOrder() {
        StopWatch stopWatch = StopWatch.createStarted();
        String orderId = "12EF4E3E-AFB0-11EB-A72F-0242C0A84429";
        Order order = service.findOne(orderId);
        ResultData<Void> resultData = service.effective(order);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }

    @Test
    void commitOrder() {
        StopWatch stopWatch = StopWatch.createStarted();

        String orderId = "1979E1C4-AFC8-11EB-B9C2-0242C0A84427";
        Order order = service.findOne(orderId);
        ResultData<Void> resultData = service.submitProcess(order);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
        System.out.println(resultData);
    }
}