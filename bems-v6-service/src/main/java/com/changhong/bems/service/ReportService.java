package com.changhong.bems.service;

import com.changhong.bems.dao.PoolReportViewDao;
import com.changhong.bems.dao.ReportMonthUsageViewDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.report.AnnualBudgetResponse;
import com.changhong.bems.entity.ReportMonthUsageView;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PoolReportViewDao poolReportViewDao;
    @Autowired
    private ReportMonthUsageViewDao monthUsageViewDao;

    /**
     * 获取年度预算分析报表数据
     *
     * @param subjectId 预算主体
     * @param year      年度
     * @return 年度预算分析报表数据结果
     */
    public List<AnnualBudgetResponse> annualBudgetAnalysis(String subjectId, Integer year, Set<String> itemCodes) {
        return poolReportViewDao.annualBudgetAnalysis(subjectId, year, itemCodes);
    }

    /**
     * 获取年度预算使用趋势报表数据
     *
     * @param subjectId 预算主体
     * @param itemCode  预算科目
     * @param years     查询年度
     * @return 年度预算分析报表数据结果
     */
    public Map<Integer, BigDecimal[]> annualUsageTrend(String subjectId, String itemCode, Set<Integer> years) {
        Map<Integer, BigDecimal[]> result = new HashMap<>();
        BigDecimal[] data;
        for (Integer year : years) {
            int i = 0;
            data = new BigDecimal[12];
            while (i < 12) {
                data[i++] = BigDecimal.ZERO;
            }
            result.put(year, data);
        }
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(ReportMonthUsageView.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(ReportMonthUsageView.FIELD_ITEM, itemCode));
        search.addFilter(new SearchFilter(ReportMonthUsageView.FIELD_YEAR, years, SearchFilter.Operator.IN));
        List<ReportMonthUsageView> list = monthUsageViewDao.findByFilters(search);
        Map<Integer, List<ReportMonthUsageView>> mapData = list.stream().collect(Collectors.groupingBy(ReportMonthUsageView::getYear, Collectors.toList()));
        List<ReportMonthUsageView> usageList;
        for (Map.Entry<Integer, List<ReportMonthUsageView>> entry : mapData.entrySet()) {
            data = result.get(entry.getKey());
            usageList = entry.getValue();
            if (CollectionUtils.isNotEmpty(usageList)) {
                for (ReportMonthUsageView usage : usageList) {
                    int index = Integer.parseInt(usage.getMonthly()) - 1;
                    if (OperationType.USE == usage.getOperation()) {
                        data[index] = data[index].add(usage.getAmount());
                    } else if (OperationType.FREED == usage.getOperation()) {
                        data[index] = data[index].subtract(usage.getAmount());
                    }
                }
            }
        }
        return result;
    }
}
