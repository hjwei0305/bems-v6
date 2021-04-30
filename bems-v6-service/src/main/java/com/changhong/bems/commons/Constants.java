package com.changhong.bems.commons;

import com.changhong.bems.dto.KeyValueDto;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-30 10:50
 */
public final class Constants {

    public static final String NONE = "none";

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    public static Set<KeyValueDto> getDimensionCodes() {
        Set<KeyValueDto> set = new LinkedHashSet<>();
        set.add(new KeyValueDto("period", "预算期间"));
        set.add(new KeyValueDto("item", "预算科目"));
        set.add(new KeyValueDto("org", "组织机构"));
        set.add(new KeyValueDto("project", "项目"));
        set.add(new KeyValueDto("udf1", "自定义1"));
        set.add(new KeyValueDto("udf2", "自定义2"));
        set.add(new KeyValueDto("udf3", "自定义3"));
        set.add(new KeyValueDto("udf4", "自定义4"));
        set.add(new KeyValueDto("udf5", "自定义5"));
        return set;
    }
}
