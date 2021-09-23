package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 项目(Project)DTO类
 *
 * @author sei
 * @since 2021-07-31 16:02:20
 */
@ApiModel(description = "项目DTO")
public class ProjectDto implements Serializable {
    private static final long serialVersionUID = -59692976042104106L;
    /**
     * 项目id
     */
    @ApiModelProperty(value = "项目id")
    private String id;
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

    public ProjectDto(String id, String code, String name) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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