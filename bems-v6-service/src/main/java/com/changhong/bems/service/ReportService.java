package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAttributeAmountDao;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.report.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
     * 预算概览报表数据
     *
     * @param request 预算概览报表数据查询
     * @return 预算概览报表数据结果
     */
    public List<OverviewVo> overview(OverviewRequest request) {
        List<OverviewVo> result = new ArrayList<>();
        List<OverviewDataItemVo> list = poolAttributeAmountDao.overview(request);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        // 将结果按年分组
        Map<Integer, List<OverviewDataItemVo>> mapData = list.stream().collect(Collectors.groupingBy(OverviewDataItemVo::getYear, Collectors.toList()));

        int index;
        OverviewVo vo;
        List<OverviewDataItemVo> itemList;
        PeriodType periodType = request.getPeriodType();
        for (Integer year : request.getYears()) {
            vo = new OverviewVo();
            // 年度
            vo.setYear(year);
            switch (periodType) {
                case ANNUAL:
                    // 可使用
                    BigDecimal balance = BigDecimal.ZERO;
                    // 已使用
                    BigDecimal used = BigDecimal.ZERO;
                    itemList = mapData.get(year);
                    if (Objects.nonNull(itemList)) {
                        for (OverviewDataItemVo dataItem : itemList) {
                            balance = balance.add(dataItem.getBalance());
                            used = used.add(dataItem.getUsed());
                        }
                    }
                    vo.setBalance(new BigDecimal[]{balance});
                    vo.setUsed(new BigDecimal[]{used});
                    break;
                case SEMIANNUAL:
                    // 可使用
                    BigDecimal[] semiannualB = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    // 已使用
                    BigDecimal[] semiannualU = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    itemList = mapData.get(year);
                    if (Objects.nonNull(itemList)) {
                        for (OverviewDataItemVo dataItem : itemList) {
                            index = dataItem.getMonth();
                            if (index <= 6) {
                                semiannualB[0] = semiannualB[0].add(dataItem.getBalance());
                                semiannualU[0] = semiannualU[0].add(dataItem.getUsed());
                            } else {
                                semiannualB[1] = semiannualB[1].add(dataItem.getBalance());
                                semiannualU[1] = semiannualU[1].add(dataItem.getUsed());
                            }
                        }
                    }
                    vo.setBalance(semiannualB);
                    vo.setUsed(semiannualU);
                    break;
                case QUARTER:
                    // 可使用
                    BigDecimal[] quarterB = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
                    // 已使用
                    BigDecimal[] quarterU = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
                    itemList = mapData.get(year);
                    if (Objects.nonNull(itemList)) {
                        for (OverviewDataItemVo dataItem : itemList) {
                            index = dataItem.getMonth();
                            if (index <= 3) {
                                quarterB[0] = quarterB[0].add(dataItem.getBalance());
                                quarterU[0] = quarterU[0].add(dataItem.getUsed());
                            } else if (index <= 6) {
                                quarterB[1] = quarterB[1].add(dataItem.getBalance());
                                quarterU[1] = quarterU[1].add(dataItem.getUsed());
                            } else if (index <= 9) {
                                quarterB[2] = quarterB[2].add(dataItem.getBalance());
                                quarterU[2] = quarterU[2].add(dataItem.getUsed());
                            } else {
                                quarterB[3] = quarterB[3].add(dataItem.getBalance());
                                quarterU[3] = quarterU[3].add(dataItem.getUsed());
                            }
                        }
                    }
                    vo.setBalance(quarterB);
                    vo.setUsed(quarterU);
                    break;
                case MONTHLY:
                    // 可使用
                    BigDecimal[] monthB = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
                    // 已使用
                    BigDecimal[] monthU = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
                    itemList = mapData.get(year);
                    if (Objects.nonNull(itemList)) {
                        for (OverviewDataItemVo dataItem : itemList) {
                            index = dataItem.getMonth() - 1;
                            monthB[index] = dataItem.getBalance();
                            monthU[index] = dataItem.getUsed();
                        }
                    }
                    vo.setBalance(monthB);
                    vo.setUsed(monthU);
                    break;
                default:
            }
            result.add(vo);
        }
        return result;
    }

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
