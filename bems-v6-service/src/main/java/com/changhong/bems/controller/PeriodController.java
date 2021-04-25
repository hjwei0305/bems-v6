package com.changhong.bems.controller;

import com.changhong.bems.api.PeriodApi;
import com.changhong.bems.dto.CreateCustomizePeriodRequest;
import com.changhong.bems.dto.CreateNormalPeriodRequest;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.Period;
import com.changhong.bems.service.PeriodService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.util.IdGenerator;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预算期间(Period)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@RestController
@Api(value = "PeriodApi", tags = "预算期间服务")
@RequestMapping(path = PeriodApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class PeriodController extends BaseEntityController<Period, PeriodDto> implements PeriodApi {
    /**
     * 预算期间服务对象
     */
    @Autowired
    private PeriodService service;

    @Override
    public BaseEntityService<Period> getService() {
        return service;
    }

    /**
     * 按预算主体获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    @Override
    public ResultData<List<PeriodDto>> findBySubject(String subjectId, String type) {
        return ResultData.success(convertToDtos(service.findBySubject(subjectId, EnumUtils.getEnum(PeriodType.class, type))));
    }

    /**
     * 通过预算期间id查询所有可用的预算期间
     * 预算池溯源使用
     * 预算期间：
     * 1.自定义期间：以“=”匹配
     * 2.非自定义期间：按枚举@see {@link PeriodType}向下匹配（年度 < 半年度 < 季度 < 月度）
     * <p>
     * 优先使用自定义 > 月度 > 季度 > 半年度 > 年度
     *
     * @param periodId 预算期间id
     * @return 预算期间清单
     */
    @Override
    public ResultData<List<PeriodDto>> findAvailablePeriods(String periodId) {
        return ResultData.success(convertToDtos(service.findAvailablePeriods(periodId)));
    }

    /**
     * 设置预算期间状态
     *
     * @param id     预算期间id
     * @param status 预算期间状态
     * @return 期间清单
     */
    @Override
    public ResultData<Void> setPeriodStatus(String id, boolean status) {
        return service.setPeriodStatus(id, status);
    }

    /**
     * 关闭过期预算期间调度定时任务
     * 定时任务执行，关闭过期预算期间
     *
     * @return 操作结果
     */
    @Override
    public ResultData<Void> closingOverduePeriod() {
        return service.closingOverduePeriod();
    }

    /**
     * 创建标准期间
     *
     * @param request 预算主体id
     * @return 期间清单
     */
    @Override
    public ResultData<Void> createNormalPeriod(CreateNormalPeriodRequest request) {
        return service.createNormalPeriod(request.getSubjectId(), request.getYear(), request.getPeriodTypes());
    }

    /**
     * 创建/编辑自定义期间
     *
     * @param request 预算主体id
     * @return 期间清单
     */
    @Override
    public ResultData<Void> saveCustomizePeriod(CreateCustomizePeriodRequest request) {
        Period period = new Period();
        period.setId(request.getId());
        period.setType(PeriodType.CUSTOMIZE);
        period.setCode(String.valueOf(IdGenerator.nextId()));
        period.setSubjectId(request.getSubjectId());
        period.setName(request.getName());
        period.setYear(request.getStartDate().getYear());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        return service.saveCustomizePeriod(period);
    }
}