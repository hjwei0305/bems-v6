package com.changhong.bems.service;

import com.changhong.bems.dto.BudgetFree;
import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
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
       String s = "{\"useList\":[{\"classification\":\"DEPARTMENT\",\"corpCode\":\"Q000\",\"amount\":5000.00,\"eventCode\":\"REIMBURSEMENT_USE\",\"bizId\":\"81EF9573-6176-11EC-9F00-0242C0A8440F\",\"bizCode\":\"R000000008-1\",\"bizRemark\":\"报销单R000000008-国内差旅费报销【黄琰】启动流程\",\"date\":\"2021-12-20\",\"item\":\"6600010000\",\"org\":\"D94C2527-5CB3-11EC-BFAE-0242C0A8440A\",\"project\":null,\"costCenter\":null,\"udf1\":null,\"udf2\":null,\"udf3\":null,\"udf4\":null,\"udf5\":null,\"bizName\":\"国内差旅费报销【黄琰】(预算科目：6600010000-费用-差旅费)预算占用\",\"application\":false}],\"freeList\":[]}";
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