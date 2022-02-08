package com.changhong.bems.service;

import com.changhong.bems.dto.BudgetFree;
import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.LocalMockUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.serial.sdk.SerialService;
import com.changhong.sei.util.thread.ThreadLocalHolder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 22:49
 */
class BudgetServiceTest extends BaseUnit5Test {
    @Autowired
    private BudgetService service;
    @Autowired
    private SerialService serialService;


    @Test
    void use1() throws InterruptedException {
        MockUser localMockUser = new LocalMockUser();
        SessionUser sessionUser = ContextUtil.getSessionUser();
        StopWatch stopWatch = new StopWatch("给号器");
        stopWatch.start("开始");

        Executor threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000; i++) {
            threadPool.execute(() -> {
                String code;
                try {
                    // 本地线程全局变量存储-开始
                    ThreadLocalHolder.begin();
                    localMockUser.mockCurrentUser(sessionUser);

                    code = serialService.getNumber(Order.class, sessionUser.getTenantCode());
                } catch (Exception e) {
                    e.printStackTrace();
                    code = "";
                } finally {
                    ThreadLocalHolder.end();
                }
                if (StringUtils.isNotBlank(code)) {
                    System.out.println(Thread.currentThread().getName() + " 结果: " + code);
                } else {
                    throw new RuntimeException("未取到code");
                }
            });
        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        TimeUnit.MINUTES.sleep(10);
    }

    @Test
    void use() {
        // BudgetRequest request = new BudgetRequest();
        // List<BudgetUse> useList = new ArrayList<>();
        // BudgetUse use = new BudgetUse();
        // use.setCorpCode("Q000");
        // use.setEventCode("REIMBURSEMENT_USE");
        // use.setAmount(new BigDecimal(17000));
        // use.setBizId(IdGenerator.uuid2());
        // use.setBizCode("USE_TEST_" + System.currentTimeMillis());
        // use.setBizRemark("测试占用");
        // use.setDate("2020-11-05");
        // use.setItem("00002");
        // use.setOrg("435B09B6-D0E1-11EA-93C3-0242C0A8460D");
        // useList.add(use);use = new BudgetUse();
        // use.setCorpCode("Q000");
        // use.setEventCode("REIMBURSEMENT_USE");
        // use.setAmount(new BigDecimal(15000));
        // use.setBizId(IdGenerator.uuid2());
        // use.setBizCode("USE_TEST_" + System.currentTimeMillis());
        // use.setBizRemark("测试占用");
        // use.setDate("2020-11-05");
        // use.setItem("00003");
        // use.setOrg("435B09B6-D0E1-11EA-93C3-0242C0A8460D");
        // useList.add(use);
        // request.setUseList(useList);
        String s = "{\"useList\":[{\"classification\":\"DEPARTMENT\",\"corpCode\":\"Q000\",\"amount\":1500.00,\"eventCode\":\"APPLICATION_USE\",\"bizId\":\"7B6101DE-7D85-11EC-A697-0242C0A8440F\",\"bizCode\":\"A000000024-1\",\"bizRemark\":\"申请单A000000024-国内差旅费报销【伏锐】启动流程\",\"date\":\"2022-01-25\",\"item\":\"6600010000\",\"org\":\"D94C2527-5CB3-11EC-BFAE-0242C0A8440A\",\"project\":null,\"costCenter\":null,\"udf1\":null,\"udf2\":null,\"udf3\":null,\"udf4\":null,\"udf5\":null,\"bizName\":\"国内差旅费报销【伏锐】(预算科目：6600010000-费用-差旅费)预算占用\",\"application\":true}],\"freeList\":[]}";
        BudgetRequest request = JsonUtils.fromJson(s, BudgetRequest.class);
        ResultData<List<BudgetResponse>> resultData = service.use(request);
        System.out.println(resultData);
    }

    @Test
    void freed() {
        BudgetRequest request = new BudgetRequest();
        List<BudgetFree> freeList = new ArrayList<>();
        BudgetFree free = new BudgetFree("USE_TEST", "USE_TEST_2");
        freeList.add(free);
        request.setFreeList(freeList);
        ResultData<List<BudgetResponse>> resultData = service.use(request);
        System.out.println(resultData);
    }
}