package com.diom.web;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Web 服务启动类
 *
 * @author diom
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        System.out.println("==================================");
        System.out.println("====  Web 服务启动成功！      ====");
        System.out.println("==================================");
    }
}

