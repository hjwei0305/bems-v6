package com.changhong.bems.service;

import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolQuickQueryParam;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageInfo;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
        // PoolQuickQueryParam queryParam = new PoolQuickQueryParam();
        // queryParam.setSubjectId("70DCA496-D2F9-11EB-8BB5-0242C0A84425");
        // queryParam.setYear(2021);
        // queryParam.setQuickSearchValue("差旅费");
        // queryParam.setPageInfo(new PageInfo());
        // List<SearchOrder> searchOrders = new ArrayList<>();
        // searchOrders.add(SearchOrder.asc("code"));
        // queryParam.setSortOrders(searchOrders);
        String s = "{\"quickSearchValue\":\"\",\"quickSearchProperties\":[\"code\",\"item\",\"itemName\",\"periodName\",\"projectName\",\"orgName\",\"udf1Name\",\"udf2Name\",\"udf3Name\",\"udf4Name\",\"udf5Name\"],\"pageInfo\":{\"page\":1,\"rows\":50},\"sortOrders\":[{\"property\":\"itemName\",\"direction\":\"ASC\"},{\"property\":\"startDate\",\"direction\":\"ASC\"}],\"year\":2021,\"periodType\":\"ANNUAL\",\"subjectId\":\"C81A4E58-BBD4-11EB-A896-0242C0A84429\"}";
        PoolQuickQueryParam queryParam = JsonUtils.fromJson(s, PoolQuickQueryParam.class);
        PageResult<PoolAttributeDto> pageResult = service.findPoolByPage(queryParam);
        System.out.println(pageResult);
    }

    @Test
    void getNextPeriodBudgetPool() {
        ResultData<Pool> pool = service.getNextPeriodBudgetPool("282CABA0-CE7A-11EB-8C70-0242C0A84429", false);
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
        List<PoolAttributeDto> resultData = service.findPoolAttributes(list);
        System.out.println(resultData);
    }
}