package com.changhong.bems.entity;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.OperationType;
import com.changhong.sei.core.entity.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实现功能：预算报表科目月度趋势
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-11 17:37
 */
@Entity
@Table(name = "view_report_month_usage")
public class ReportMonthUsageView extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 5332232947791784203L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_YEAR = "year";
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 预算科目
     */
    @Column(name = "item_code", updatable = false)
    protected String item = Constants.NONE;
    /**
     * 操作类型
     */
    @Column(name = "operation_type", updatable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operation;
    /**
     * 金额
     */
    @Column(name = "amount", updatable = false)
    private BigDecimal amount = BigDecimal.ZERO;
    /**
     * 所属年度
     */
    @Column(name = "year")
    private Integer year;
    /**
     * 所属月度
     */
    @Column(name = "monthly")
    private String monthly;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getMonthly() {
        return monthly;
    }

    public void setMonthly(String monthly) {
        this.monthly = monthly;
    }
}
