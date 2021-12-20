package com.changhong.bems.commons;

import com.changhong.bems.dto.KeyValueDto;
import com.changhong.sei.core.context.ContextUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-30 10:50
 */
public final class Constants {
    private static final Set<KeyValueDto> DEFAULT_DIMENSION_CODES;

    public static final String NONE = "none";

    public static final String DIMENSION_CODE_PERIOD = "period";
    public static final String DIMENSION_CODE_ITEM = "item";
    public static final String DIMENSION_CODE_ORG = "org";
    public static final String DIMENSION_CODE_PROJECT = "project";
    public static final String DIMENSION_CODE_COST_CENTER = "costCenter";
    public static final String DIMENSION_CODE_UDF1 = "udf1";
    public static final String DIMENSION_CODE_UDF2 = "udf2";
    public static final String DIMENSION_CODE_UDF3 = "udf3";
    public static final String DIMENSION_CODE_UDF4 = "udf4";
    public static final String DIMENSION_CODE_UDF5 = "udf5";

    static {
        DEFAULT_DIMENSION_CODES = new LinkedHashSet<>();
        // 预算期间
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_PERIOD, ContextUtil.getMessage("default_dimension_period")));
        // 预算科目
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_ITEM, ContextUtil.getMessage("default_dimension_item")));
        // 组织机构
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_ORG, ContextUtil.getMessage("default_dimension_org")));
        // 项目
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_PROJECT, ContextUtil.getMessage("default_dimension_project")));
        // 成本中心
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_COST_CENTER, ContextUtil.getMessage("default_dimension_cost_center")));
        // 自定义维度
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF1, ContextUtil.getMessage("default_dimension_udf1")));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF2, ContextUtil.getMessage("default_dimension_udf2")));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF3, ContextUtil.getMessage("default_dimension_udf3")));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF4, ContextUtil.getMessage("default_dimension_udf4")));
        DEFAULT_DIMENSION_CODES.add(new KeyValueDto(DIMENSION_CODE_UDF5, ContextUtil.getMessage("default_dimension_udf5")));
    }

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

    /**
     * redis key 预算策略缓存key
     */
    public static final String STRATEGY_CACHE_KEY_PREFIX = "bems-v6:strategy:";

    /**
     * redis key 预算维度缓存key
     */
    public static final String DIMENSION_CACHE_KEY_PREFIX = "bems-v6:dimension";

    /**
     * redis key 维度主体缓存key
     */
    public static final String DIMENSION_SUBJECT_CACHE_KEY_PREFIX = "bems-v6:dimension:subject";

    /**
     * redis key 维度name与value映射缓存key
     */
    public static final String DIMENSION_MAP_CACHE_KEY_PREFIX = "bems-v6:dimension:map:";

}
