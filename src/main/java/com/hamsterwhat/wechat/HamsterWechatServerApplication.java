package com.hamsterwhat.wechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.hamsterwhat.wechat")
@MapperScan(basePackages = {"com.hamsterwhat.wechat.mapper"})
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class HamsterWechatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamsterWechatServerApplication.class, args);
    }

}

