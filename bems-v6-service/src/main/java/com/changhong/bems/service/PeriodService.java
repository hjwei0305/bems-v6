package com.changhong.bems.service;

import com.changhong.bems.dao.PeriodDao;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.Period;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 预算期间(Period)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@Service
public class PeriodService extends BaseEntityService<Period> {
    @Autowired
    private PeriodDao dao;

    @Override
    protected BaseEntityDao<Period> getDao() {
        return dao;
    }

    /**
     * 按预算主体获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    public List<Period> findBySubject(String subjectId, PeriodType type) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Period.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Period.FIELD_TYPE, type));
        return dao.findByFilters(search);
    }

    /**
     * 关闭预算期间
     *
     * @param ids 预算期间id
     * @return 期间清单
     */
    public ResultData<Void> closePeriods(List<String> ids) {
        return null;
    }

    /**
     * 创建标准期间
     *
     * @return 期间清单
     */
    public ResultData<Void> createNormalPeriod(String subjectId, int year, PeriodType[] periodTypes) {
        return null;
    }

    /**
     * 创建/编辑自定义期间
     *
     * @param period 自定义预算期间
     * @return 期间清单
     */
    public ResultData<Void> saveCustomizePeriod(Period period) {
        return null;
    }
}