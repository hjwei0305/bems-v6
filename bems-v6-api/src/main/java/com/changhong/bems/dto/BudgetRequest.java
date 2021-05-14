package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现功能：预算占用
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
@ApiModel(description = "预算占用")
public class BudgetRequest implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    @ApiModelProperty(value = "预算占用清单")
    private List<BudgetUse> useList;
    @ApiModelProperty(value = "预算释放清单")
    private List<BudgetFree> freeList;

    public List<BudgetUse> getUseList() {
        return useList;
    }

    public BudgetRequest setUseList(List<BudgetUse> useList) {
        this.useList = useList;
        return this;
    }

    public List<BudgetFree> getFreeList() {
        return freeList;
    }

    public BudgetRequest setFreeList(List<BudgetFree> freeList) {
        this.freeList = freeList;
        return this;
    }

    public void addUse(BudgetUse use) {
        List<BudgetUse> useList = getUseList();
        if (useList == null || useList.size() == 0) {
            useList = new ArrayList<>();
        }
        useList.add(use);
        this.useList = useList;
    }

    public void addFree(BudgetFree free) {
        List<BudgetFree> freeList = getFreeList();
        if (freeList == null || freeList.size() == 0) {
            freeList = new ArrayList<>();
        }
        freeList.add(free);
        this.freeList = freeList;
    }
}
