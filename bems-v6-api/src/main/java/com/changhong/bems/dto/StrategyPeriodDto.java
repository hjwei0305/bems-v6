package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.changhong.sei.core.dto.serializer.EnumJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 预算期间策略(SubjectPeriod)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@ApiModel(description = "预算期间策略DTO")
public class StrategyPeriodDto extends BaseEntityDto {
    private static final long serialVersionUID = -85112390830826629L;

    /**
     * 预算主体id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 期间分类
     */
    @JsonSerialize(using = EnumJsonSerializer.class)
    @ApiModelProperty(value = "期间类型")
    private PeriodType periodType;
    /**
     * 允许使用(业务可用)
     */
    @ApiModelProperty(value = "允许使用(业务可用)")
    private Boolean use = Boolean.FALSE;
    /**
     * 允许结转
     */
    @ApiModelProperty(value = "允许结转")
    private Boolean roll = Boolean.FALSE;
    /**
     * 冻结
     */
    @ApiModelProperty(value = "是否冻结")
    private Boolean frozen = Boolean.FALSE;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public Boolean getRoll() {
        return roll;
    }

    public void setRoll(Boolean roll) {
        this.roll = roll;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

}