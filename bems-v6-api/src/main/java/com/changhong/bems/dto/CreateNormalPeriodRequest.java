package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 16:56
 */
@ApiModel(description = "创建标准期间DTO")
public class CreateNormalPeriodRequest implements Serializable {
    private static final long serialVersionUID = -6612468820644741725L;

    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    @ApiModelProperty(value = "年份", example = "2020")
    private int year;
    @ApiModelProperty(value = "要创建的期间类型")
    private PeriodType[] periodTypes;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public PeriodType[] getPeriodTypes() {
        return periodTypes;
    }

    public void setPeriodTypes(PeriodType[] periodTypes) {
        this.periodTypes = periodTypes;
    }
}
