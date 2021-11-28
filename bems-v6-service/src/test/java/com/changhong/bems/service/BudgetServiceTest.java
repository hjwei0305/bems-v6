package com.changhong.bems.service;

import com.changhong.bems.dto.use.BudgetFree;
import com.changhong.bems.dto.use.BudgetRequest;
import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.bems.dto.use.BudgetUse;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 22:49
 */
class BudgetServiceTest extends BaseUnit5Test {
    @Autowired
    private BudgetService service;

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
       String s = "{\"useList\":[{\"corpCode\":\"Q000\",\"amount\":110011.00,\"eventCode\":\"APPLICATION_USE\",\"bizId\":\"D19BB8EE-4C2F-11EC-90D4-0242C0A8441F\",\"bizCode\":\"A002148870-1\",\"bizRemark\":\"申请单A002148870-压测用差旅费457【李继斌】启动流程\",\"date\":\"2021-11-24\",\"item\":\"1000000457\",\"org\":\"06222AB7-2D92-11EC-9959-0242C0A84406\",\"project\":null,\"udf1\":null,\"udf2\":null,\"udf3\":null,\"udf4\":null,\"udf5\":null,\"bizName\":\"压测用差旅费457【李继斌】(预算科目：1000000457-new压测用科目_457)预算占用\",\"application\":true}],\"freeList\":[]}";
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