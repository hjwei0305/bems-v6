package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算维度属性(DimensionAttribute)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@ApiModel(description = "预算维度属性DTO")
public class DimensionAttributeDto extends BaseAttributeDto {
    private static final long serialVersionUID = 874180418915412119L;
    /**
     * 预算主体id
     */
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 属性
     */
    @ApiModelProperty(value = "属性")
    private String attribute;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

}