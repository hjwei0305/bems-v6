package com.changhong.bems.service;

import com.changhong.bems.entity.StrategyItem;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-29 18:09
 */
class StrategyItemServiceTest extends BaseUnit5Test {
    @Autowired
    private StrategyItemService service;

    @Test
    void delete() {
    }

    @Test
    void save() {
    }

    @Test
    void findBySubject() {
    }

    @Test
    void findBySubjectUnfrozen() {
    }

    @Test
    void frozen() {
    }

    @Test
    void getUnassigned() {
    }

    @Test
    void getAssigned() {
        String json = "{\"quickSearchValue\":\"\",\"quickSearchProperties\":[\"code\",\"name\"],\"pageInfo\":{\"page\":1,\"rows\":30},\"filters\":[{\"fieldName\":\"subjectId\",\"operator\":\"EQ\",\"value\":\"C81A4E58-BBD4-11EB-A896-0242C0A84429\"},{\"fieldName\":\"id\",\"operator\":\"NOTIN\",\"value\":[\"0364279D-C8F5-11EB-B646-0242C0A8442C\"]}]}";
        Search search = JsonUtils.fromJson(json, Search.class);
        PageResult<StrategyItem> pageResult = service.getAssigned(search);
        System.out.println(pageResult);
    }

    @Test
    void assigne() {
    }

    @Test
    void checkReference() {
    }

    @Test
    void reference() {
    }

    @Test
    void getSubjectItem() {
    }
}