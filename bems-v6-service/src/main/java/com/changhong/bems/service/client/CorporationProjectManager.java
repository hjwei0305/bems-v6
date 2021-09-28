package com.changhong.bems.service.client;

import com.changhong.bems.dto.ProjectDto;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.entity.BaseEntity;
import com.changhong.sei.core.entity.IFrozen;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 17:35
 */
@Component
public class CorporationProjectManager {

    private final CorporationProjectClient client;

    public CorporationProjectManager(CorporationProjectClient client) {
        this.client = client;
    }

    /**
     * 分页获取公司项目
     *
     * @return 分页查询结果
     */
    public ResultData<List<ProjectDto>> findByPage(String erpCorporationCode, String searchValue, Set<String> excludeIds) {
        ResultData<List<ProjectDto>> result;
        Search search = new Search();
        // 未冻结的
        search.addFilter(new SearchFilter(CorporationProjectDto.FIELD_ERP_CORPORATION_CODE, erpCorporationCode));
        if (CollectionUtils.isNotEmpty(excludeIds)) {
            search.addFilter(new SearchFilter(BaseEntity.ID, excludeIds, SearchFilter.Operator.NOTIN));
        }
        search.addFilter(new SearchFilter(IFrozen.FROZEN, Boolean.FALSE));
        if (StringUtils.isNotBlank(searchValue)) {
            search.addQuickSearchProperty(CorporationProjectDto.FIELD_WBS_PROJECT_CODE);
            search.addQuickSearchProperty(CorporationProjectDto.FIELD_WBS_PROJECT_NAME);
            search.addQuickSearchProperty(CorporationProjectDto.FIELD_INNER_ORDER_CODE);
            search.addQuickSearchProperty(CorporationProjectDto.FIELD_INNER_ORDER_NAME);
            search.setQuickSearchValue(searchValue);
        }
        // 设置分页参数
        // search.setPageInfo(pageInfo);
        // 设置排序
        search.addSortOrder(new SearchOrder(CorporationProjectDto.FIELD_WBS_PROJECT_CODE));
        search.addSortOrder(new SearchOrder(CorporationProjectDto.FIELD_INNER_ORDER_CODE));

        ResultData<PageResult<CorporationProjectDto>> resultData = client.findByPage(search);
        if (resultData.successful()) {
            PageResult<CorporationProjectDto> projectPageResult = resultData.getData();
            // PageResult<ProjectDto> pageResult = new PageResult<>(projectPageResult);
            List<ProjectDto> projectList = new ArrayList<>();
            List<CorporationProjectDto> dtoList = projectPageResult.getRows();
            for (CorporationProjectDto dto : dtoList) {
                if (StringUtils.isNotBlank(dto.getWbsProjectCode())) {
                    projectList.add(new ProjectDto(dto.getId(), dto.getWbsProjectCode(), dto.getWbsProjectName()));
                } else if (StringUtils.isNotBlank(dto.getInnerOrderCode())) {
                    projectList.add(new ProjectDto(dto.getId(), dto.getInnerOrderCode(), dto.getInnerOrderName()));
                }
            }
            // pageResult.setRows(projectList);
            result = ResultData.success(projectList);
        } else {
            result = ResultData.fail(resultData.getMessage());
        }
        return result;
    }


    /**
     * 按ERP公司代码查询项目
     *
     * @param erpCorpCode ERP公司代码
     * @return 项目清单
     */
    public ResultData<List<ProjectDto>> findByErpCode(String erpCorpCode) {
        ResultData<List<CorporationProjectDto>> result = client.findByErpCode(erpCorpCode);
        if (result.successful()) {
            List<ProjectDto> projectList = new ArrayList<>();
            List<CorporationProjectDto> dtoList = result.getData();
            if (CollectionUtils.isNotEmpty(dtoList)) {
                for (CorporationProjectDto dto : dtoList) {
                    if (StringUtils.isNotBlank(dto.getWbsProjectCode())) {
                        projectList.add(new ProjectDto(dto.getId(), dto.getWbsProjectCode(), dto.getWbsProjectName()));
                    } else if (StringUtils.isNotBlank(dto.getInnerOrderCode())) {
                        projectList.add(new ProjectDto(dto.getId(), dto.getInnerOrderCode(), dto.getInnerOrderName()));
                    }
                }
            }
            return ResultData.success(projectList);
        } else {
            return ResultData.fail(result.getMessage());
        }
    }
}
