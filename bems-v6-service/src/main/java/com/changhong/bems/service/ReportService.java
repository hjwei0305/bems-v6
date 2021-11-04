package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAttributeAmountDao;
import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisVo;
import com.changhong.bems.dto.report.UsageTrendRequest;
import com.changhong.bems.entity.PoolAttributeAmount;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-09 15:01
 */
@Service
public class ReportService {
    @Autowired
    private PoolAttributeAmountDao poolAttributeAmountDao;

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    public List<ExecutionAnalysisVo> executionAnalysis(ExecutionAnalysisRequest request) {
        return poolAttributeAmountDao.executionAnalysis(request);
    }

    /**
     * 预算使用趋势报表数据
     *
     * @param request 查询
     * @return 预算使用趋势报表数据结果
     */
    public Map<Integer, BigDecimal[]> usageTrend(UsageTrendRequest request) {
        Map<Integer, BigDecimal[]> result = new HashMap<>();
        BigDecimal[] data;
        for (Integer year : request.getYear()) {
            int i = 0;
            data = new BigDecimal[12];
            while (i < 12) {
                data[i++] = BigDecimal.ZERO;
            }
            result.put(year, data);
        }
        List<PoolAttributeAmount> list = poolAttributeAmountDao.usageTrend(request);
        Map<Integer, List<PoolAttributeAmount>> mapData = list.stream().collect(Collectors.groupingBy(PoolAttributeAmount::getYear, Collectors.toList()));
        List<PoolAttributeAmount> usageList;
        for (Map.Entry<Integer, List<PoolAttributeAmount>> entry : mapData.entrySet()) {
            data = result.get(entry.getKey());
            usageList = entry.getValue();
            if (CollectionUtils.isNotEmpty(usageList)) {
                for (PoolAttributeAmount usage : usageList) {
                    data[usage.getMonth() - 1] = usage.getUsedAmount();
                }
            }
        }
        return result;
    }
}
