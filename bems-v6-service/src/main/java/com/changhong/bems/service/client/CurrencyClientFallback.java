package com.changhong.bems.service.client;

import com.changhong.bems.dto.CurrencyDto;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-22 09:35
 */
@Component
public class CurrencyClientFallback implements CurrencyClient {
    /**
     * 获取所有未冻结币种
     *
     * @return 未冻结币种清单
     */
    @Override
    public ResultData<List<CurrencyDto>> findAllUnfrozen() {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_002"));
    }
}
