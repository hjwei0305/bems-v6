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

    public static final String DIMENSION_CODE_PERIOD = "period";
    public static final String DIMENSION_CODE_ITEM = "item";
    public static final String DIMENSION_CODE_ORG = "org";
    public static final String DIMENSION_CODE_PROJECT = "project";
    public static final String DIMENSION_CODE_UDF1 = "udf1";
    public static final String DIMENSION_CODE_UDF2 = "udf2";
    public static final String DIMENSION_CODE_UDF3 = "udf3";
    public static final String DIMENSION_CODE_UDF4 = "udf4";
    public static final String DIMENSION_CODE_UDF5 = "udf5";

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    public static Set<KeyValueDto> getDimensionCodes() {
        Set<KeyValueDto> set = new LinkedHashSet<>();
        set.add(new KeyValueDto(DIMENSION_CODE_PERIOD, "预算期间"));
        set.add(new KeyValueDto(DIMENSION_CODE_ITEM, "预算科目"));
        set.add(new KeyValueDto(DIMENSION_CODE_ORG, "组织机构"));
        set.add(new KeyValueDto(DIMENSION_CODE_PROJECT, "项目"));
        set.add(new KeyValueDto(DIMENSION_CODE_UDF1, "自定义1"));
        set.add(new KeyValueDto(DIMENSION_CODE_UDF2, "自定义2"));
        set.add(new KeyValueDto(DIMENSION_CODE_UDF3, "自定义3"));
        set.add(new KeyValueDto(DIMENSION_CODE_UDF4, "自定义4"));
        set.add(new KeyValueDto(DIMENSION_CODE_UDF5, "自定义5"));
        return set;
    }

    /**
     * 预算确认事件
     */
    public static final String EVENT_BUDGET_CONFIRM = "BUDGET_CONFIRM";
    /**
     * 预算撤销确认事件
     */
    public static final String EVENT_BUDGET_CANCEL = "BUDGET_CANCEL_CONFIRM";
    /**
     * 预算生效释放预占用事件
     */
    public static final String EVENT_BUDGET_FREED = "BUDGET_CONFIRM_FREED";
    /**
     * 预算生效事件
     */
    public static final String EVENT_BUDGET_EFFECTIVE = "BUDGET_EFFECTIVE";
    /**
     * 预算滚动结转事件
     */
    public static final String EVENT_BUDGET_TRUNDLE = "BUDGET_TRUNDLE";
    /**
     * 预算滚动结转事件
     */
    public static final String EVENT_BUDGET_FREEZE = "BUDGET_FREEZE";
    /**
     * 预算滚动结转事件
     */
    public static final String EVENT_BUDGET_UNFREEZE = "BUDGET_UNFREEZE";

    /**
     * redis key 订单处理状态缓存key
     */
    public static final String HANDLE_CACHE_KEY_PREFIX = "bems-v6:order:handle:";
}
