package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAttributeAmountDao;
import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisVo;
import com.changhong.bems.dto.report.UsageTrendRequest;
import com.changhong.bems.dto.report.UsageTrendVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        List<UsageTrendVo> list = poolAttributeAmountDao.usageTrend(request);
        Map<Integer, List<UsageTrendVo>> mapData = list.stream().collect(Collectors.groupingBy(UsageTrendVo::getYear, Collectors.toList()));
        List<UsageTrendVo> usageList;
        for (Map.Entry<Integer, List<UsageTrendVo>> entry : mapData.entrySet()) {
            data = result.get(entry.getKey());
            usageList = entry.getValue();
            if (CollectionUtils.isNotEmpty(usageList)) {
                for (UsageTrendVo usage : usageList) {
                    data[usage.getMonth() - 1] = usage.getAmount();
                }
            }
        }
        return result;
    }
}
