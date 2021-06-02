package com.changhong.bems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * REST服务主程序
 *
 * @author 马超(Vision.Mac)
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.changhong.bems.service.client"})
public class RestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }
}
