package com.changhong.bems.service;

import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        String json = "{\"id\":\"6B5AC673-52B2-11EC-94F8-0242C0A84427\",\"name\":\"1111\",\"corporationCode\":\"Q000\",\"corporationName\":\"四川虹信软件股份有限公司\",\"classification\":\"DEPARTMENT\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"strategyId\":\"limitExecutionStrategy\",\"strategyName\":\"强控\",\"rank\":0,\"frozen\":false,\"tenantCode\":\"10044\",\"orgList\":[{\"name\":\"四川虹信软件股份有限公司\",\"namePath\":\"/四川长虹电子控股集团有限公司/消费者BG/长虹多媒体公司/智慧业务BG/四川虹信软件股份有限公司\",\"id\":\"435B09B6-D0E1-11EA-93C3-0242C0A8460D\"}]}";
        Subject subject = JsonUtils.fromJson(json, Subject.class);
        OperateResultWithData<Subject> result = service.save(subject);
        System.out.println(result);
    }

    @Test
    void getSubject() {
    }
}