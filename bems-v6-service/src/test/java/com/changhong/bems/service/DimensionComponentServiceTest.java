package com.changhong.bems.service;

import com.changhong.bems.dto.ProjectDto;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-11 09:21
 */
class DimensionComponentServiceTest extends BaseUnit5Test {
    @Autowired
    private DimensionComponentService service;

    @Test
    void getBudgetItems() {
    }

    @Test
    void getPeriods() {
    }

    @Test
    void getOrgTree() {
    }

    @Test
    void getProjects() {
        String subjectId = "C81A4E58-BBD4-11EB-A896-0242C0A84429";
        ResultData<List<ProjectDto>> resultData = service.getProjects(subjectId, "test", null);
        System.out.println(resultData);
    }

    @Test
    void getDimensionValues() {

    }
}