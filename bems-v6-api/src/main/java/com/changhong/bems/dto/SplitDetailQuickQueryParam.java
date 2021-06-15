package com.changhong.bems.dto;

import com.changhong.sei.core.dto.serach.QuickQueryParam;
import com.changhong.sei.core.dto.serach.SearchFilter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-03 18:29
 */
@ApiModel("预算分解上级期间的分页查询参数")
public class SplitDetailQuickQueryParam extends QuickQueryParam {
    private static final long serialVersionUID = -8903760059949186885L;

    /**
     * 订单id
     */
    @NotBlank
    @ApiModelProperty(value = "订单id", required = true)
    private String orderId;

    /**
     * 筛选字段
     */
    private List<SearchFilter> filters;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }
}
