package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;

/**
 * 项目(Project)DTO类
 *
 * @author sei
 * @since 2021-07-31 16:02:20
 */
@ApiModel(description = "项目DTO")
public class ProjectDto extends BaseEntityDto {
    private static final long serialVersionUID = -59692976042104106L;
    /**
     * 关联的WBS项目编号
     */
    @Size(max = 30)
    @ApiModelProperty(value = "项目编号")
    private String code;
    /**
     * 名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "项目名称")
    private String name;

    public ProjectDto() {
    }

    public ProjectDto(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}