package com.changhong.bems.service;

import com.changhong.bems.dto.CorporationDto;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-01 22:37
 */
class SubjectServiceTest extends BaseUnit5Test {

    @Autowired
    private SubjectService service;

    @Test
    void findCurrencies() {
    }

    @Test
    void findUserAuthorizedCorporations() {
        ResultData<List<CorporationDto>> resultData = service.findUserAuthorizedCorporations();
        System.out.println(resultData);
    }

    @Test
    void findOrgTree() {
    }

    @Test
    void getOrgTree() {
    }

    @Test
    void getOrgChildren() {
    }

    @Test
    void getSubjectOrganizations() {
    }

    @Test
    void save() {
        String json = "{\n" +
                "    \"currencyCode\": \"CNY\",\n" +
                "    \"strategyId\": \"excessExecutionStrategy\",\n" +
                "    \"corporationCode\": \"Q600\",\n" +
                "    \"corporationName\": \"四川智远乐享软件有限公司\",\n" +
                "    \"name\": \"费用报销\",\n" +
                "    \"currencyName\": \"人民币\",\n" +
                "    \"strategyName\": \"弱控\",\n" +
                "    \"frozen\": false,\n" +
                "    \"orgList\": [\n" +
                "        {\n" +
                "            \"name\": \"四川虹信智远软件有限公司\",\n" +
                "            \"namePath\": \"/四川长虹电子控股集团有限公司/消费者BG/长虹多媒体公司/智慧业务BG/四川虹信软件股份有限公司/四川虹信智远软件有限公司\",\n" +
                "            \"id\": \"5C4E36E9-D0E1-11EA-93C3-0242C0A8460D\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"classification\": \"DEPARTMENT\"\n" +
                "}";
        Subject subject = JsonUtils.fromJson(json, Subject.class);
        OperateResultWithData<Subject> result = service.save(subject);
        System.out.println(result);
    }

    @Test
    void getSubject() {
    }
}