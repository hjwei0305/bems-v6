package com.changhong.bems.service.client;

import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import org.springframework.stereotype.Component;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-22 09:27
 */
@Component
public class CorporationProjectClientFallback implements CorporationProjectClient {

    /**
     * 分页获取公司项目
     */
    @Override
    public ResultData<PageResult<CorporationProjectDto>> findByPage(Search search) {
        // 公司项目接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_004"));
    }
}
