package com.changhong.bems.commons;

import com.changhong.bems.dto.BaseAttributeDto;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Pool;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-19 16:33
 */
public class PoolHelper {

    public static PoolAttributeDto constructPoolAttribute(Pool pool) {
        PoolAttributeDto dto = new PoolAttributeDto();
        dto.setId(pool.getId());
        dto.setCode(pool.getCode());
        dto.setSubjectId(pool.getSubjectId());
        dto.setCurrencyCode(pool.getCurrencyCode());
        dto.setCurrencyName(pool.getCurrencyName());
        dto.setManageOrg(pool.getManageOrg());
        dto.setManageOrgName(pool.getManageOrgName());
        dto.setPeriodType(pool.getPeriodType());
        dto.setYear(pool.getYear());
        dto.setStartDate(pool.getStartDate());
        dto.setEndDate(pool.getEndDate());
        dto.setActived(pool.getActived());
        dto.setUse(pool.getUse());
        dto.setRoll(pool.getRoll());
        dto.setDelay(pool.getDelay());
        dto.setTotalAmount(pool.getTotalAmount());
        dto.setUsedAmount(pool.getUsedAmount());
        dto.setBalance(pool.getBalance());
        return dto;
    }


    public static void putAttribute(BaseAttributeDto dto, DimensionAttribute attribute) {
        dto.setAttribute(attribute.getAttribute());
        dto.setAttributeCode(attribute.getAttributeCode());
        dto.setPeriod(attribute.getPeriod());
        dto.setPeriodName(attribute.getPeriodName());
        dto.setItem(attribute.getItem());
        dto.setItemName(attribute.getItemName());
        dto.setOrg(attribute.getOrg());
        dto.setOrgName(attribute.getOrgName());
        dto.setProject(attribute.getProject());
        dto.setProjectName(attribute.getProjectName());
        dto.setUdf1(attribute.getUdf1());
        dto.setUdf1Name(attribute.getUdf1Name());
        dto.setUdf2(attribute.getUdf2());
        dto.setUdf2Name(attribute.getUdf2Name());
        dto.setUdf3(attribute.getUdf3());
        dto.setUdf3Name(attribute.getUdf3Name());
        dto.setUdf4(attribute.getUdf4());
        dto.setUdf4Name(attribute.getUdf4Name());
        dto.setUdf5(attribute.getUdf5());
        dto.setUdf5Name(attribute.getUdf5Name());
    }
}
