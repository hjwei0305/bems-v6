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

    public static final Set<KeyValueDto> DEFAULT_DIMENSION_CODES;

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    public static Set<KeyValueDto> getDimensionCodes() {
        return DEFAULT_DIMENSION_CODES;
    }

    /**
     * 预算注入
     */
    public static final String EVENT_BUDGET_INJECTION = "BUDGET_INJECTION";
    /**
     * 预算调整
     */
    public static final String EVENT_BUDGET_ADJUSTMENT = "BUDGET_ADJUSTMENT";
    /**
     * 预算分解
     */
    public static final String EVENT_BUDGET_SPLIT = "BUDGET_SPLIT";
    /**
     * 预算注入撤销
     */
    public static final String EVENT_BUDGET_INJECTION_CANCEL = "BUDGET_INJECTION_CANCEL";
    /**
     * 预算调整撤销
     */
    public static final String EVENT_BUDGET_ADJUSTMENT_CANCEL = "BUDGET_ADJUSTMENT_CANCEL";
    /**
     * 预算分解撤销
     */
    public static final String EVENT_BUDGET_SPLIT_CANCEL = "BUDGET_SPLIT_CANCEL";
    /**
     * 预算滚动结转事件
     */
    public static final String EVENT_BUDGET_TRUNDLE = "BUDGET_TRUNDLE";
    /**
     * 预算冻结事件
     */
    public static final String EVENT_BUDGET_FREEZE = "BUDGET_FREEZE";
    /**
     * 预算解冻事件
     */
    public static final String EVENT_BUDGET_UNFREEZE = "BUDGET_UNFREEZE";

    /**
     * redis key 订单处理状态缓存key
     */
    public static final String HANDLE_CACHE_KEY_PREFIX = "bems-v6:order:handle:";

    static {
        DEFAULT_DIMENSION_CODES = new LinkedHashSet<>();
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_PERIOD, "预算期间"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_ITEM, "预算科目"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_ORG, "组织机构"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_PROJECT, "项目"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF1, "自定义1"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF2, "自定义2"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF3, "自定义3"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF4, "自定义4"));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF5, "自定义5"));
    }
}
