package com.changhong.bems.dto;

import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-16 16:00
 */
@ApiModel(description = "科目执行策略查询对象")
public class SubjectItemSearch extends Search implements Serializable {

    private static final long serialVersionUID = -2358290293249757935L;

    @NotBlank
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
