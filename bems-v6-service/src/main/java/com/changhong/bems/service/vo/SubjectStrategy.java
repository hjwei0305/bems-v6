package com.changhong.bems.service.vo;

import com.changhong.bems.entity.Strategy;

import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 11:15
 */
public class SubjectStrategy {
    private final String subjectId;
    private final String strategyId;
    private final String strategyCode;
    private final String strategyName;
    private final String strategyClass;
    private final int level;

    public SubjectStrategy(String subjectId, Strategy strategy) {
        this.subjectId = subjectId;
        this.strategyId = strategy.getId();
        this.strategyCode = strategy.getCode();
        this.strategyName = strategy.getName();
        this.strategyClass = strategy.getClassPath();
        this.level = strategy.getRank();
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public String getStrategyCode() {
        return strategyCode;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public String getStrategyClass() {
        return strategyClass;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubjectStrategy that = (SubjectStrategy) o;

        if (!Objects.equals(subjectId, that.subjectId)) {
            return false;
        }
        return Objects.equals(strategyId, that.strategyId);
    }

    @Override
    public int hashCode() {
        int result = subjectId != null ? subjectId.hashCode() : 0;
        result = 31 * result + (strategyId != null ? strategyId.hashCode() : 0);
        return result;
    }
}
