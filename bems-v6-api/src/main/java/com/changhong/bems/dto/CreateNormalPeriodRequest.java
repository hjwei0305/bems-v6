package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    @Pattern(regexp = "^([1-9][0-9]{4})$", message = "年份不正确")
    @ApiModelProperty(value = "年份", example = "2020", required = true)
    private int year;
    @NotNull
    @ApiModelProperty(value = "要创建的期间类型", required = true)
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
