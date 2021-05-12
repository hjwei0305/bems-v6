package com.changhong.bems.service.client;

import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-12 16:41
 */
class FlowClientTest extends BaseUnit5Test {
    @Autowired
    private FlowClient client;

    @Test
    void signalByBusinessId() {
        String orderId = "A6F3C0F5-B2FB-11EB-83F7-0242C0A84427";
        String taskActDefId = "ReceiveTask_2";
        ResultData<Void> resultData= client.signalByBusinessId(orderId, taskActDefId, new HashMap<>());
        System.out.println(resultData);
    }

    @Test
    void endByBusinessId() {
        String orderId = "60857DE5-B2E8-11EB-9956-0242C0A84429";
        ResultData<Void> resultData= client.endByBusinessId(orderId);
        System.out.println(resultData);
    }
}