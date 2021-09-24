package com.changhong.bems.service;

import com.changhong.bems.dao.OrderConfigDao;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.OrderConfig;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 预算配置(OrderConfig)业务逻辑实现类
 *
 * @author sei
 * @since 2021-09-24 09:12:59
 */
@Service
public class OrderConfigService extends BaseEntityService<OrderConfig> {
    @Autowired
    private OrderConfigDao dao;

    @Override
    protected BaseEntityDao<OrderConfig> getDao() {
        return dao;
    }

    /**
     * 按订单类型获取配置的期间类型
     *
     * @param category 订单类型
     * @return 配置的期间类型清单
     */
    public Set<PeriodType> findPeriodTypes(OrderCategory category) {
        Set<PeriodType> periodTypes;
        List<OrderConfig> configList = dao.findListByProperty(OrderConfig.FIELD_ORDER_CATEGORY, category);
        if (CollectionUtils.isNotEmpty(configList)) {
            periodTypes = configList.stream().map(OrderConfig::getPeriodType).collect(Collectors.toSet());
        } else {
            periodTypes = new HashSet<>();
        }
        return periodTypes;
    }

    /**
     * 按订单类型获取配置
     *
     * @param category 订单类型
     * @return 配置清单
     */
    public List<OrderConfig> findByOrderCategory(OrderCategory category) {
        List<OrderConfig> configList = dao.findListByProperty(OrderConfig.FIELD_ORDER_CATEGORY, category);
        if (CollectionUtils.isEmpty(configList)) {
            configList = findAllConfigs().stream().filter(c -> category == c.getOrderCategory()).collect(Collectors.toList());
        }
        return configList;
    }

    /**
     * 获取所有订单配置
     *
     * @return 获取所有预算订单配置
     */
    public List<OrderConfig> findAllConfigs() {
        List<OrderConfig> list = dao.findAll();
        if (CollectionUtils.isEmpty(list)) {
            list = init();
        }
        return list;
    }

    public synchronized List<OrderConfig> init() {
        String tenantCode = ContextUtil.getTenantCode();
        List<OrderConfig> configList = new ArrayList<>();
        OrderConfig config = new OrderConfig();
        config.setOrderCategory(OrderCategory.INJECTION);
        config.setPeriodType(PeriodType.ANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.INJECTION);
        config.setPeriodType(PeriodType.SEMIANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.INJECTION);
        config.setPeriodType(PeriodType.QUARTER);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.INJECTION);
        config.setPeriodType(PeriodType.MONTHLY);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.INJECTION);
        config.setPeriodType(PeriodType.CUSTOMIZE);
        config.setTenantCode(tenantCode);
        configList.add(config);

        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.ADJUSTMENT);
        config.setPeriodType(PeriodType.ANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.ADJUSTMENT);
        config.setPeriodType(PeriodType.SEMIANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.ADJUSTMENT);
        config.setPeriodType(PeriodType.QUARTER);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.ADJUSTMENT);
        config.setPeriodType(PeriodType.MONTHLY);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.ADJUSTMENT);
        config.setPeriodType(PeriodType.CUSTOMIZE);
        config.setTenantCode(tenantCode);
        configList.add(config);

        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.SPLIT);
        config.setPeriodType(PeriodType.ANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.SPLIT);
        config.setPeriodType(PeriodType.SEMIANNUAL);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.SPLIT);
        config.setPeriodType(PeriodType.QUARTER);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.SPLIT);
        config.setPeriodType(PeriodType.MONTHLY);
        config.setTenantCode(tenantCode);
        configList.add(config);
        config = new OrderConfig();
        config.setOrderCategory(OrderCategory.SPLIT);
        config.setPeriodType(PeriodType.CUSTOMIZE);
        config.setTenantCode(tenantCode);
        configList.add(config);
        dao.save(configList);
        return configList;
    }

    /**
     * 订单配置启用
     *
     * @param id     订单配置
     * @param enable 启用状态ø
     * @return 结果
     */
    public ResultData<Void> updateConfig(String id, boolean enable) {
        OrderConfig config = dao.findOne(id);
        if (Objects.isNull(config)) {
            return ResultData.fail(ContextUtil.getMessage("order_config_001"));
        }
        config.setEnable(enable);
        dao.save(config);
        return ResultData.success();
    }
}