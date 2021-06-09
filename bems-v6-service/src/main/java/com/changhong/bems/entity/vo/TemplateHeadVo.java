package com.changhong.bems.entity.vo;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-08 17:48
 */
public class TemplateHeadVo implements Serializable {
    private static final long serialVersionUID = -1976973217540963994L;
    private final int index;
    private final String filed;
    private final String value;

    public TemplateHeadVo(int index, String filed, String value) {
        this.index = index;
        this.filed = filed;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getFiled() {
        return filed;
    }

    public String getValue() {
        return value;
    }
}
