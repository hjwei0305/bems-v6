package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 预算事件(Event)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@ApiModel(description = "预算事件DTO")
public class EventDto extends BaseEntityDto implements Serializable {
    private static final long serialVersionUID = -57036484686343107L;

    /**
     * 代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "代码", required = true)
    private String code;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    /**
     * 标签名(多个用逗号分隔)
     */
    @ApiModelProperty(value = "标签名(多个用逗号分隔)")
    private String label;
    /**
     * 业务来源
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "业务来源", required = true)
    private String bizFrom;
    /**
     * 冻结
     */
    @ApiModelProperty(value = "冻结")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 系统必要
     */
    @ApiModelProperty(value = "系统必要")
    private Boolean required = Boolean.FALSE;
    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private Integer rank = 0;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBizFrom() {
        return bizFrom;
    }

    public void setBizFrom(String bizFrom) {
        this.bizFrom = bizFrom;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}