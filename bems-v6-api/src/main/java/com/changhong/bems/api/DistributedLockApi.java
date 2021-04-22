package com.changhong.bems.api;

import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 实现功能：分布式锁演示
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-01-27 11:19
 */
@FeignClient(name = "bems-v6", path = DistributedLockApi.PATH)
public interface DistributedLockApi {
    String PATH = "demo";

    @GetMapping(value = "lock")
    @ApiOperation(value = "分布式锁演示", notes = "分布式锁演示")
    ResultData<String> lock(@RequestParam("key") String key);

    @GetMapping(value = "lockNonParam")
    @ApiOperation(value = "分布式锁演示无入参", notes = "分布式锁演示无入参")
    ResultData<String> lockNonParam();
}
