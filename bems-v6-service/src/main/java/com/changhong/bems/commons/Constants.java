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
     * 预算下达生效事件
     */
    public static final String EVENT_INJECTION_EFFECTIVE = "INJECTION_EFFECTIVE";
    /**
     * 预算调整生效事件
     */
    public static final String EVENT_ADJUSTMENT_EFFECTIVE = "ADJUSTMENT_EFFECTIVE";
    /**
     * 预算分解生效事件
     */
    public static final String EVENT_SPLIT_EFFECTIVE = "SPLIT_EFFECTIVE";
    /**
     * 预算下达提交流程事件
     */
    public static final String EVENT_INJECTION_SUBMIT = "INJECTION_SUBMIT";
    /**
     * 预算调整提交流程事件
     */
    public static final String EVENT_ADJUSTMENT_SUBMIT = "ADJUSTMENT_SUBMIT";
    /**
     * 预算分解提交流程事件
     */
    public static final String EVENT_SPLIT_SUBMIT = "SPLIT_SUBMIT";
    /**
     * 预算下达终止流程事件
     */
    public static final String EVENT_INJECTION_CANCEL = "INJECTION_CANCEL";
    /**
     * 预算调整终止流程事件
     */
    public static final String EVENT_ADJUSTMENT_CANCEL = "ADJUSTMENT_CANCEL";
    /**
     * 预算分解终止流程事件
     */
    public static final String EVENT_SPLIT_CANCEL = "SPLIT_CANCEL";
    /**
     * 预算下达流程完成事件
     */
    public static final String EVENT_INJECTION_COMPLETE = "INJECTION_COMPLETE";
    /**
     * 预算调整流程完成事件
     */
    public static final String EVENT_ADJUSTMENT_COMPLETE = "ADJUSTMENT_COMPLETE";
    /**
     * 预算分解流程完成事件
     */
    public static final String EVENT_SPLIT_COMPLETE = "SPLIT_COMPLETE";

    /**
     * 消息队列处理类型-订单确认
     */
    public static final String ORDER_OPERATION_CONFIRM = "CONFIRM";
    /**
     * 消息队列处理类型-订单撤销确认
     */
    public static final String ORDER_OPERATION_CANCEL = "CANCEL";
    /**
     * 消息队列处理类型-订单生效
     */
    public static final String ORDER_OPERATION_EFFECTIVE = "EFFECTIVE";

    /**
     * redis key 订单处理状态缓存key
     */
    public static final String HANDLE_CACHE_KEY_PREFIX = "bems-v6:order:handle:";

    /**
     * 发布/订阅 的 Topic
     */
    public static final String TOPIC = "bems-v6:order:state";
}
