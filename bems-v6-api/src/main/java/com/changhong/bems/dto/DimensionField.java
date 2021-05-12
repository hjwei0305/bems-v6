package com.changhong.bems.dto;

import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-12 17:24
 */
public class DimensionField implements Serializable {
    private static final long serialVersionUID = 7757329416159723042L;
    private String dimension;
    private String value;
    private String title;

    public DimensionField() {

    }

    public DimensionField(String dimension, String title) {
        this.dimension = dimension;
        this.title = title;
        this.value = dimension + "Name";
    }

    public String getDimension() {
        return dimension;
    }

    public String getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }
}
