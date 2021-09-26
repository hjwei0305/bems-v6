package com.changhong.bems.service;

import com.changhong.bems.dao.OrderConfigDao;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.OrderConfig;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 预算配置(OrderConfig)业务逻辑实现类
 *
 * @author sei
 * @since 2021-09-24 09:12:59
 */
@Service
public class OrderConfigService {
    @Autowired
    private OrderConfigDao dao;

    /**
     * 按订单类型获取配置的期间类型
     *
     * @param categoryId 订单类型id
     * @return 配置的期间类型清单
     */
    public OrderCategory[] findPeriodTypes(String categoryId) {
        OrderCategory[] categories;
        List<OrderConfig> configList = dao.findListByProperty(OrderConfig.FIELD_CATEGORY_ID, categoryId);
        if (CollectionUtils.isNotEmpty(configList)) {
            int i = 0;
            categories = new OrderCategory[configList.size()];
            for (OrderConfig config : configList) {
                categories[i++] = config.getOrderCategory();
            }
        } else {
            categories = new OrderCategory[0];
        }
        return categories;
    }

    /**
     * 按订单类型获取配置的期间类型
     *
     * @param categoryIds 订单类型id清单
     * @return 配置的期间类型清单
     */
    public Map<String, OrderCategory[]> findPeriodTypes(Set<String> categoryIds) {
        Map<String, OrderCategory[]> result = new HashMap<>();
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(OrderConfig.FIELD_CATEGORY_ID, categoryIds, SearchFilter.Operator.IN));
        List<OrderConfig> configList = dao.findByFilters(search);
        Map<String, List<OrderConfig>> dataMap = configList.stream().collect(Collectors.groupingBy(OrderConfig::getCategoryId, Collectors.toList()));
        List<OrderConfig> list;
        OrderCategory[] orderCategories;
        for (Map.Entry<String, List<OrderConfig>> entry : dataMap.entrySet()) {
            list = entry.getValue();
            if (CollectionUtils.isNotEmpty(list)) {
                orderCategories = new OrderCategory[list.size()];
                int i = 0;
                for (OrderConfig config : list) {
                    orderCategories[i++] = config.getOrderCategory();
                }
                result.put(entry.getKey(), orderCategories);
            }
        }
        return result;
    }

    /**
     * 按订单类型获取配置的期间类型
     *
     * @param category 订单类型
     * @return 配置的期间类型清单
     */
    public Set<PeriodType> findPeriodTypes(Set<String> categoryIds, OrderCategory category) {
        Set<PeriodType> periodTypes;
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(OrderConfig.FIELD_CATEGORY_ID, categoryIds, SearchFilter.Operator.IN));
        search.addFilter(new SearchFilter(OrderConfig.FIELD_ORDER_CATEGORY, category));
        List<OrderConfig> configList = dao.findByFilters(search);
        if (CollectionUtils.isNotEmpty(configList)) {
            periodTypes = configList.stream().map(OrderConfig::getPeriodType).collect(Collectors.toSet());
        } else {
            periodTypes = new HashSet<>();
        }
        return periodTypes;
    }

    /**
     * 添加预算订单配置
     *
     * @param categoryId      预算类型id
     * @param periodType      预算期间类型
     * @param orderCategories 预算订单类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void putConfigData(String categoryId, PeriodType periodType, OrderCategory[] orderCategories) {
        List<OrderConfig> configs = dao.findListByProperty(OrderConfig.FIELD_CATEGORY_ID, categoryId);
        if (CollectionUtils.isNotEmpty(configs)) {
            dao.deleteAll(configs);
            configs.clear();
        }

        OrderConfig config;
        configs = new ArrayList<>();
        String tenantCode = ContextUtil.getTenantCode();
        for (OrderCategory category : orderCategories) {
            config = new OrderConfig();
            config.setCategoryId(categoryId);
            config.setPeriodType(periodType);
            config.setOrderCategory(category);
            config.setTenantCode(tenantCode);
            configs.add(config);
        }
        dao.save(configs);
    }

}