package com.changhong.bems.service;

import com.changhong.bems.dto.PoolLogDto;
import com.changhong.bems.entity.PoolLog;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 12:45
 */
class PoolLogServiceTest extends BaseUnit5Test {
    @Autowired
    private PoolLogService service;

    @Test
    void getUseRecords() {
    }

    @Test
    void addLogRecord() {
    }

    @Test
    void updateFreed() {
    }

    @Test
    void findByPage() {
        PageResult<PoolLogDto> pageResult = service.findByPage(Search.createSearch());
        System.out.println(pageResult);
    }
}