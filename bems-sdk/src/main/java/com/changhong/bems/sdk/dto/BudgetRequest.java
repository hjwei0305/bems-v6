package com.changhong.bems.sdk.dto;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现功能：预算占用
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
public class BudgetRequest implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    /**
     * 预算占用清单
     */
    @Valid
    private List<BudgetUse> useList;
    /**
     * 预算释放清单
     */
    @Valid
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
        if (this.useList == null) {
            this.useList = new ArrayList<>();
        }
        this.useList.add(use);
    }

    public void addFree(BudgetFree free) {
        if (this.freeList == null) {
            this.freeList = new ArrayList<>();
        }
        this.freeList.add(free);
    }
}
