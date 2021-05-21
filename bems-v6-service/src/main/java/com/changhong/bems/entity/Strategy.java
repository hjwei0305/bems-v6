package com.changhong.bems.entity;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算策略(Strategy)实体类
 *
 * @author sei
 * @since 2021-04-22 11:12:01
 */
@Entity
@Table(name = "strategy")
@DynamicInsert
@DynamicUpdate
public class Strategy extends BaseAuditableEntity implements ICodeUnique, Serializable {
    private static final long serialVersionUID = -28243258893909771L;
    public static final String FIELD_CATEGORY = "category";
    /**
     * 策略代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 策略名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 策略类路径
     */
    @Column(name = "class_path")
    private String classPath;
    /**
     * 策略类别
     */
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private StrategyCategory category;
    /**
     * 策略描述
     */
    @Column(name = "remark")
    private String remark;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public StrategyCategory getCategory() {
        return category;
    }

    public void setCategory(StrategyCategory category) {
        this.category = category;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}