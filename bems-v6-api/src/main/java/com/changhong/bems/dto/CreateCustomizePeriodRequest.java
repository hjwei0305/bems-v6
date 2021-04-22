package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 16:56
 */
@ApiModel(description = "自定义期间DTO")
public class CreateCustomizePeriodRequest extends BaseEntityDto implements Serializable {
    private static final long serialVersionUID = -6612468820644741725L;

    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    @ApiModelProperty(value = "期间名称")
    private String name;
    /**
     * 起始日期
     */
    @ApiModelProperty(value = "起始日期", example = "2021-04-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    /**
     * 截止日期
     */
    @ApiModelProperty(value = "截止日期", example = "2021-04-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
