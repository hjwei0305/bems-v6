package com.changhong.bems.service.client;

import com.changhong.bems.dto.CorporationDto;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-22 09:27
 */
@Component
public class CorporationClientFallback implements CorporationClient {

    /**
     * 获取当前用户有权限的公司清单(未冻结)
     *
     * @param featureCode 功能项代码
     * @return 有权限的公司数据清单
     */
    @Override
    public ResultData<List<CorporationDto>> getUserAuthorizedEntities(String featureCode) {
        // 公司接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_001"));
    }
}
