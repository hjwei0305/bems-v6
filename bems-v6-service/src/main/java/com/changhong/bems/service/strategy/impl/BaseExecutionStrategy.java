package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.vo.PoolLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 18:29
 */
public abstract class BaseExecutionStrategy {

    /**
     * 首先按期间类型下标进行排序: 下标值越大优先级越高
     */
    protected Map<String, PoolLevel> sortByPeriod(List<PoolAttributeView> pools) {
        Map<String, PoolLevel> poolLevelMap = new HashMap<>();
        PoolLevel level;
        String poolCode;
        for (PoolAttributeView pool : pools) {
            poolCode = pool.getCode();
            level = poolLevelMap.get(poolCode);
            if (Objects.isNull(level)) {
                level = new PoolLevel(pool.getSubjectId(), poolCode, pool.getAttributeCode());
            }
            level.setLevel(level.getLevel() + pool.getPeriodType().ordinal());
            poolLevelMap.put(poolCode, level);
        }
        return poolLevelMap;
    }
}
