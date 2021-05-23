package com.changhong.bems.service;

import com.changhong.bems.dto.BudgetFree;
import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        BudgetRequest request = new BudgetRequest();
        List<BudgetUse> useList = new ArrayList<>();
        BudgetUse use = new BudgetUse();
        use.setCorpCode("Q000");
        use.setEventCode("USE_TEST");
        use.setAmount(60);
        use.setBizId("USE_TEST_2");
        use.setBizCode("USE_TEST_2");
        use.setBizRemark("测试占用");
        use.setDate("2021-05-23");
        use.setItem("2000");
        use.setOrg("A62E9175-D0E1-11EA-93C3-0242C0A8460D");
        useList.add(use);
        request.setUseList(useList);
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