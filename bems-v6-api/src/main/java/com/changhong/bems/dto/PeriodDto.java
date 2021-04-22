package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Date;

/**
 * 预算期间(Period)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@ApiModel(description = "预算期间DTO")
public class PeriodDto extends BaseEntityDto {
    private static final long serialVersionUID = 632696081380214947L;
    /**
     * 预算主体id
     */
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 代码
     */
    @ApiModelProperty(value = "代码")
    private String code;
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * 期间分类
     */
    @ApiModelProperty(value = "期间分类")
    private PeriodType type;
    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;
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
    /**
     * 是否已关闭
     */
    @ApiModelProperty(value = "是否已关闭")
    private Boolean closed;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PeriodType getType() {
        return type;
    }

    public void setType(PeriodType type) {
        this.type = type;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

}