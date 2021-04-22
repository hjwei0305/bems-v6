package com.changhong.bems.entity;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * (Strategy)实体类
 *
 * @author sei
 * @since 2021-04-22 11:12:01
 */
@Entity
@Table(name = "strategy")
@DynamicInsert
@DynamicUpdate
public class Strategy extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = -28243258893909771L;
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

}