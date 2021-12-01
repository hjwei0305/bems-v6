package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.SubjectDimensionDao;
import com.changhong.bems.dto.Classification;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.entity.Subject;
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
    @Autowired
    private SubjectService subjectService;

    /**
     * 按预算分类获取维度清单
     *
     * @param classification 预算分类
     * @return 维度清单
     */
    public List<DimensionDto> getDimensionsByClassification(Classification classification) {
        List<DimensionDto> dimensionList = new ArrayList<>();
        List<Dimension> dimensions = dimensionService.findAll();
        if (CollectionUtils.isNotEmpty(dimensions)) {
            DimensionDto dto;
            for (Dimension dimension : dimensions) {
                // 组织级预算主体无项目维度
                if (Objects.equals(Classification.DEPARTMENT, classification)) {
                    if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimension.getCode())) {
                        continue;
                    }
                    if (StringUtils.equals(Constants.DIMENSION_CODE_COST_CENTER, dimension.getCode())) {
                        continue;
                    }
                }
                // 成本中心级预算无组织维度
                else if (Objects.equals(Classification.COST_CENTER, classification)) {
                    if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimension.getCode())) {
                        continue;
                    }
                }
                dto = new DimensionDto();
                dto.setCode(dimension.getCode());
                dto.setName(dimension.getName());
                dto.setStrategyId(dimension.getStrategyId());
                dto.setStrategyName(dimension.getStrategyName());
                dto.setUiComponent(dimension.getUiComponent());
                dto.setRequired(dimension.getRequired());
                dto.setRank(dimension.getRank());
                dimensionList.add(dto);
            }
        }
        return dimensionList;
    }

    /**
     * 按预算主体获取维度清单
     *
     * @param subjectId 预算主体id
     * @return 查询结果
     */
    public List<DimensionDto> getDimensions(String subjectId) {
        Subject subject = subjectService.findOne(subjectId);
        if (Objects.nonNull(subject)) {
            List<DimensionDto> dimensionList = this.getDimensionsByClassification(subject.getClassification());
            if (CollectionUtils.isNotEmpty(dimensionList)) {
                List<SubjectDimension> subjectDimensions = dao.findListByProperty(SubjectDimension.FIELD_SUBJECT_ID, subjectId);
                if (CollectionUtils.isNotEmpty(subjectDimensions)) {
                    for (SubjectDimension sd : subjectDimensions) {
                        for (DimensionDto dimension : dimensionList) {
                            if (StringUtils.equals(dimension.getCode(), sd.getCode())) {
                                dimension.setId(sd.getId());
                                dimension.setStrategyId(sd.getStrategyId());
                                dimension.setStrategyName(strategyService.getNameByCode(sd.getStrategyId()));
                            }
                        }
                    }
                }
                return dimensionList;
            }
        }
        return new ArrayList<>();
    }

    @Cacheable(key = "#subjectId + ':' + #code")
    public DimensionDto getDimension(String subjectId, String code) {
        DimensionDto dto = null;
        Dimension dimension = dimensionService.findByCode(code);
        if (Objects.nonNull(dimension)) {
            dto = new DimensionDto();
            dto.setCode(dimension.getCode());
            dto.setName(dimension.getName());
            dto.setStrategyId(dimension.getStrategyId());
            dto.setStrategyName(dimension.getStrategyName());
            dto.setUiComponent(dimension.getUiComponent());
            dto.setRequired(dimension.getRequired());
            dto.setRank(dimension.getRank());

            Search search = Search.createSearch();
            search.addFilter(new SearchFilter(SubjectDimension.FIELD_SUBJECT_ID, subjectId));
            search.addFilter(new SearchFilter(SubjectDimension.FIELD_CODE, code));
            SubjectDimension subjectDimension = dao.findOneByFilters(search);
            if (Objects.nonNull(subjectDimension) && StringUtils.equals(dto.getCode(), subjectDimension.getCode())) {
                dto.setId(subjectDimension.getId());
                dto.setStrategyId(subjectDimension.getStrategyId());
                dto.setStrategyName(strategyService.getNameByCode(subjectDimension.getStrategyId()));
            }
        }
        return dto;
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
