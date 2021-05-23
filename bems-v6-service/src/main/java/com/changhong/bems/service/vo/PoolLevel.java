package com.changhong.bems.service.vo;

import java.util.Objects;

/**
 * 实现功能：根据执行策略定义预算池使用优先级
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 11:36
 */
public class PoolLevel {
    private final String subjectId;
    private final String poolCode;
    private final long attributeCode;
    private int level = 0;
    private double balance = 0;
    private double useAmount = 0;

    public PoolLevel(String subjectId, String poolCode, long attributeCode) {
        this.subjectId = subjectId;
        this.poolCode = poolCode;
        this.attributeCode = attributeCode;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public long getAttributeCode() {
        return attributeCode;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getUseAmount() {
        return useAmount;
    }

    public void setUseAmount(double useAmount) {
        this.useAmount = useAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PoolLevel poolLevel = (PoolLevel) o;

        if (!Objects.equals(subjectId, poolLevel.subjectId)) {
            return false;
        }
        return Objects.equals(poolCode, poolLevel.poolCode);
    }

    @Override
    public int hashCode() {
        int result = subjectId != null ? subjectId.hashCode() : 0;
        result = 31 * result + (poolCode != null ? poolCode.hashCode() : 0);
        return result;
    }
}
