package com.changhong.bems.service.client;

import com.changhong.bems.dto.CorporationDto;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 17:35
 */
@Component
public class CorporationManager {

    private final CorporationClient client;

    public CorporationManager(CorporationClient client) {
        this.client = client;
    }

    /**
     * 分页查询公司数据
     *
     * @return 分页查询结果
     */
    public ResultData<List<CorporationDto>> findUserAuthorizedCorporations() {
        return client.getUserAuthorizedEntities(null);
    }
}
