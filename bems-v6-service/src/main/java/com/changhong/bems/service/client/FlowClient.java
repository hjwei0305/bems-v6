package com.changhong.bems.service.client;

import com.changhong.sei.core.dto.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 实现功能：FLOW接口api
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 17:32
 */
@FeignClient(name = "flow-service", path = "flowInstance")
public interface FlowClient {

    /**
     * 接收任务回调接口
     * 预算检查占用完成后,回调流程通知之前暂挂的任务继续执行
     *
     * @param orderId      订单id
     * @param taskActDefId 任务回调id
     * @return 操作结果
     */
    @PostMapping(path = "signalByBusinessId")
    ResultData<Void> signalByBusinessId(@RequestParam(value = "businessId") String orderId,
                                        @RequestParam(value = "receiveTaskActDefId") String taskActDefId, Map<String, Object> v);

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param orderId 业务单据id
     * @return 操作结果
     */
    @PostMapping("endByBusinessId/{businessId}")
    ResultData<Void> endByBusinessId(@PathVariable("businessId") String orderId);
}
