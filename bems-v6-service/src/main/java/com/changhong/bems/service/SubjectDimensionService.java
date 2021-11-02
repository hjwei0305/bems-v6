package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectDimensionDao;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.entity.SubjectDimension;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-02 14:57
 */
@Service
@CacheConfig(cacheNames = SubjectDimensionService.CACHE_KEY)
public class SubjectDimensionService {
    public static final String CACHE_KEY = "bems-v6:dimension:subject";
    @Autowired
    private SubjectDimensionDao dao;
    @Autowired
    private DimensionService dimensionService;
    @Autowired
    private StrategyService strategyService;

    /**
     * 按预算主体获取维度清单
     *
     * @param subjectId 预算主体id
     * @return 查询结果
     */
    @Cacheable(key = "#subjectId")
    public List<DimensionDto> getDimensions(String subjectId) {
        List<DimensionDto> dimensionList = new ArrayList<>();

        List<Dimension> dimensions = dimensionService.findAll();
        if (CollectionUtils.isNotEmpty(dimensions)) {
            List<SubjectDimension> subjectDimensions = dao.findListByProperty(SubjectDimension.FIELD_SUBJECT_ID, subjectId);
            DimensionDto dto;
            for (Dimension dimension : dimensions) {
                dto = new DimensionDto();
                dto.setCode(dimension.getCode());
                dto.setName(dimension.getName());
                dto.setStrategyId(dimension.getStrategyId());
                dto.setStrategyName(dimension.getStrategyName());
                dto.setUiComponent(dimension.getUiComponent());
                dto.setRequired(dimension.getRequired());
                dto.setRank(dimension.getRank());
                if (CollectionUtils.isNotEmpty(subjectDimensions)) {
                    for (SubjectDimension sd : subjectDimensions) {
                        if (StringUtils.equals(dto.getCode(), sd.getCode())) {
                            dto.setId(sd.getId());
                            dto.setStrategyId(sd.getStrategyId());
                            dto.setStrategyName(strategyService.getNameByCode(sd.getStrategyId()));
                        }
                    }
                }
                dimensionList.add(dto);
            }
        }
        return dimensionList;
    }

    /**
     * 设置预算维度为主体私有
     *
     * @param subjectId 预算主体id
     * @param code      预算维度代码
     * @param isPrivate 是否设为私有
     * @return 设置结果
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> setSubjectDimension(String subjectId, String code, boolean isPrivate) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(SubjectDimension.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(SubjectDimension.FIELD_CODE, code));
        SubjectDimension subjectDimension = dao.findOneByFilters(search);
        if (isPrivate) {
            // 如果存在直接返回
            if (Objects.isNull(subjectDimension)) {
                Dimension dimension = dimensionService.findByCode(code);
                if (Objects.isNull(dimension)) {
                    // 维度[{0}]不存在!
                    return ResultData.fail(ContextUtil.getMessage("dimension_00002", code));
                }
                subjectDimension = new SubjectDimension();
                subjectDimension.setTenantCode(ContextUtil.getTenantCode());
                subjectDimension.setSubjectId(subjectId);
                subjectDimension.setCode(code);
                subjectDimension.setStrategyId(dimension.getStrategyId());
                dao.save(subjectDimension);
            }
        } else {
            // 如果存在则删除
            if (Objects.nonNull(subjectDimension)) {
                dao.delete(subjectDimension);
            }
        }
        return ResultData.success();
    }

    /**
     * 配置预算主体维度策略
     *
     * @param id         id
     * @param strategyId 维度策略
     * @return 配置结果
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> setDimensionStrategy(String id, String strategyId) {
        SubjectDimension subjectDimension = dao.findOne(id);
        if (Objects.nonNull(subjectDimension)) {
            subjectDimension.setStrategyId(strategyId);
            dao.save(subjectDimension);
        }
        return ResultData.success();
    }
}
