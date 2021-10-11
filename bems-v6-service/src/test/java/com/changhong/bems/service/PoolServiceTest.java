package com.changhong.bems.service;

import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-12 00:07
 */
class PoolServiceTest extends BaseUnit5Test {
    @Autowired
    private PoolService service;

    @Test
    void getPool() {
    }

    @Test
    void createPool() {
    }

    @Test
    void getPoolBalanceById() {
    }

    @Test
    void getPoolBalanceByCode() {
    }

    @Test
    void getPoolBalance() {
    }

    @Test
    void recordLog() {
    }

    @Test
    void findPoolByPage() {
        String json = "{\"quickSearchValue\":\"\",\"quickSearchProperties\":[\"code\",\"item\",\"itemName\",\"periodName\",\"projectName\",\"orgName\",\"udf1Name\",\"udf2Name\",\"udf3Name\",\"udf4Name\",\"udf5Name\"],\"pageInfo\":{\"page\":1,\"rows\":50},\"sortOrders\":[{\"property\":\"itemName\",\"direction\":\"ASC\"},{\"property\":\"startDate\",\"direction\":\"ASC\"}],\"filters\":[{\"fieldName\":\"subjectId\",\"operator\":\"EQ\",\"value\":\"C81A4E58-BBD4-11EB-A896-0242C0A84429\"},{\"fieldName\":\"periodType\",\"operator\":\"EQ\",\"value\":\"MONTHLY\"},{\"fieldName\":\"startDate\",\"operator\":\"GT\",\"value\":\"2021-01-16\",\"fieldType\":\"date8\"},{\"fieldName\":\"endDate\",\"operator\":\"LT\",\"value\":\"2021-01-16\",\"fieldType\":\"date8\"}]}";
        Search search = JsonUtils.fromJson(json, Search.class);
        PageResult<PoolAttributeView> pageResult = service.findPoolByPage(search);
        System.out.println(pageResult);
    }

    @Test
    void getNextPeriodBudgetPool() {
        ResultData<PoolAttributeView> pool = service.getNextPeriodBudgetPool("282CABA0-CE7A-11EB-8C70-0242C0A84429", false);
        System.out.println(pool);
    }

    @Test
    void trundlePool() {
        ResultData<String> resultData = service.trundlePool();
        System.out.println(resultData);
    }

    @Test
    void findPoolAttributes() {
        List<String> list = new ArrayList<>();
        list.add("0000000145");
        list.add("0000000163");
        List<PoolAttributeView> resultData = service.findPoolAttributes(list);
        System.out.println(resultData);
    }
}