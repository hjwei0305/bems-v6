package com.changhong.bems.service.client;

import com.changhong.bems.dto.CurrencyDto;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 17:30
 */
@Component
public class CurrencyManager {
    private final CurrencyClient client;

    public CurrencyManager(CurrencyClient client) {
        this.client = client;
    }

    /**
     * 获取所有未冻结币种
     *
     * @return 未冻结币种清单
     */
    public ResultData<List<CurrencyDto>> findAllUnfrozen() {
        return client.findAllUnfrozen();
    }
}
