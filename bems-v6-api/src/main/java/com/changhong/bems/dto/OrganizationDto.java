package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 实现功能: 组织机构DTO
 *
 * @author 王锦光 wangjg
 * @version 2020-01-20 16:17
 */
@ApiModel(description = "组织机构DTO")
public class OrganizationDto implements Serializable {
    private static final long serialVersionUID = 5889624803970290804L;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private String id;
    /**
     * 组织机构代码
     */
    @ApiModelProperty(value = "组织机构代码")
    private String code;

    /**
     * 组织机构名称
     */
    @ApiModelProperty(value = "组织机构名称")
    private String name;

    /**
     * 层级
     */
    @ApiModelProperty(value = "层级")
    private Integer nodeLevel = 0;

    /**
     * 代码路径
     */
    @ApiModelProperty(value = "代码路径")
    private String codePath;

    /**
     * 名称路径
     */
    @ApiModelProperty(value = "名称路径")
    private String namePath;

    /**
     * 父节点Id
     */
    @ApiModelProperty(value = "父节点Id")
    private String parentId;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private Integer rank = 0;

    private List<OrganizationDto> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(Integer nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getNamePath() {
        return namePath;
    }

    public void setNamePath(String namePath) {
        this.namePath = namePath;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public List<OrganizationDto> getChildren() {
        return children;
    }

    public void setChildren(List<OrganizationDto> children) {
        this.children = children;
    }
}
