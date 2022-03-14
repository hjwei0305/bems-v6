package com.changhong.bems.dto;

import com.changhong.sei.core.dto.serach.Search;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-03-14 08:55
 */
public class OrderSearch extends Search {
    private static final long serialVersionUID = -9124292946467831919L;

    private Boolean includeOther = Boolean.FALSE;

    public Boolean isIncludeOther() {
        return includeOther;
    }

    public void setIncludeOther(Boolean includeOther) {
        this.includeOther = includeOther;
    }
}
