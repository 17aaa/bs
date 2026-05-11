package com.zhm.springboot.userservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 主启动类
 */
@SpringBootApplication
@MapperScan(basePackages = {
        "com.zhm.springboot.userservice.mapper",
        "com.zhm.springboot.userservice.nft.mapper",
        "com.zhm.springboot.userservice.market.mapper",
        "com.zhm.springboot.userservice.wallet.mapper",
        "com.zhm.springboot.userservice.fantoken.mapper",
        "com.zhm.springboot.userservice.blockchain.mapper",
        "com.zhm.springboot.userservice.notification.mapper",
        "com.zhm.springboot.userservice.audit.mapper"
})
@EnableFeignClients
@EnableScheduling
@EnableAsync
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
