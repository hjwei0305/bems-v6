package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 实现功能：键值对DTO
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 14:39
 */
@ApiModel(description = "键值对DTO")
public class KeyValueDto implements Serializable {
    private static final long serialVersionUID = -1298549744583800386L;

    @ApiModelProperty(value = "key")
    private String key;
    @ApiModelProperty(value = "值")
    private String value;

    private String code;

    public KeyValueDto() {
    }

    public KeyValueDto(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KeyValueDto(String key, String value, String code) {
        this.key = key;
        this.value = value;
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyValueDto that = (KeyValueDto) o;

        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
