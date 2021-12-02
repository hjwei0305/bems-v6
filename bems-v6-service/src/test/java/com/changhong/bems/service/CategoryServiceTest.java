package com.changhong.bems.service;

import com.changhong.bems.dto.DimensionDto;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-01 13:33
 */
class CategoryServiceTest extends BaseUnit5Test {
    @Autowired
    private CategoryService service;

    @Test
    void findByGeneral() {
    }

    @Test
    void findBySubject() {
    }

    @Test
    void findDimensionBySubject() {
        String subjectId = "060C72D7-532F-11EC-A5BE-0242C0A84425";

        List<DimensionDto> list = service.findDimensionBySubject(subjectId);
        System.out.println(list);
    }

    @Test
    void reference() {
    }

    @Test
    void frozen() {
    }

    @Test
    void getUnassigned() {
    }

    @Test
    void getAssigned() {
        String categoryId = "9F285BB6-1EA2-11EC-8984-0242C0A84427";
        List<DimensionDto> list = service.getAssigned(categoryId);
        System.out.println(list);
    }

    @Test
    void assigne() {
    }

    @Test
    void unassigne() {
    }

    @Test
    void getByCategory() {
    }
}