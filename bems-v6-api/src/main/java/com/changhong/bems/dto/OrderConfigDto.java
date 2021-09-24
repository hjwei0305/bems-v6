package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.changhong.sei.core.dto.serializer.EnumJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算配置(Config)DTO类
 *
 * @author sei
 * @since 2021-09-24 09:13:00
 */
@ApiModel(description = "预算配置DTO")
public class OrderConfigDto extends BaseEntityDto {
    private static final long serialVersionUID = 617224814786267668L;
    /**
     * 订单类型
     */
    @JsonSerialize(using = EnumJsonSerializer.class)
    @ApiModelProperty(value = "订单类型")
    private OrderCategory orderCategory;
    /**
     * 期间类型
     */
    @JsonSerialize(using = EnumJsonSerializer.class)
    @ApiModelProperty(value = "期间类型")
    private PeriodType periodType;
    /**
     * 是否冻结
     */
    @ApiModelProperty(value = "是否启用")
    private Boolean enable;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(OrderCategory orderCategory) {
        this.orderCategory = orderCategory;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}