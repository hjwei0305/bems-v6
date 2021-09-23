package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：项目查询DTO
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-23 16:18
 */
@ApiModel(description = "项目查询DTO")
public class QueryProjectRequest implements Serializable {
    private static final long serialVersionUID = -8837599475420038468L;
    /**
     * 预算主体
     */
    @NotBlank
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 项目快速查询值
     */
    @ApiModelProperty(value = "项目快速查询值")
    private String searchValue;
    /**
     * 排除的项目id
     */
    @ApiModelProperty(value = "排除的项目id")
    private Set<String> excludeIds;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public Set<String> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(Set<String> excludeIds) {
        this.excludeIds = excludeIds;
    }
}
