package com.changhong.bems.dto.report;

import com.changhong.bems.dto.PeriodType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 16:43
 */
@ApiModel(description = "预算概览分析查询")
public class OverviewRequest implements Serializable {
    private static final long serialVersionUID = -5853079393192807149L;
    /**
     * 预算主体id
     */
    @NotBlank
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 所属年度
     */
    @NotEmpty
    @ApiModelProperty(value = "所属年度")
    private List<Integer> years;
    /**
     * 期间类型
     */
    @NotNull
    @ApiModelProperty(value = "期间类型")
    private PeriodType periodType;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
}
