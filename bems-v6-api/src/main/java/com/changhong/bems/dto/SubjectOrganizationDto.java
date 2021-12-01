package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-28 13:31
 */
public class SubjectOrganizationDto extends BaseEntityDto {
    private static final long serialVersionUID = 4084271190277172473L;
    /**
     * 预算主体id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 组织ID
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "组织ID", required = true)
    private String orgId;
    /**
     * 组织代码
     */
    @Size(max = 30)
    @ApiModelProperty(value = "组织代码")
    private String orgCode;
    /**
     * 组织名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "组织名称")
    private String orgName;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
