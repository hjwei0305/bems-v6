package com.changhong.bems.sdk;

import com.changhong.bems.sdk.client.BudgetApiClient;
import com.changhong.bems.sdk.client.BudgetItemApiClient;
import com.changhong.bems.sdk.client.BudgetPoolClient;
import com.changhong.bems.sdk.manager.BudgetUseManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 实现功能：开发工具包配置类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-04-20 22:41
 */
@Configuration
@EnableFeignClients(basePackages = {"com.changhong.bems.sdk.client"})
public class BudgetSdkAutoConfig {

    @Bean
    public BudgetUseManager budgetUseManager(BudgetApiClient budgetApi,
                                             BudgetItemApiClient itemApi,
                                             BudgetPoolClient poolApi) {
        return new BudgetUseManager(budgetApi, itemApi, poolApi);
    }

}
