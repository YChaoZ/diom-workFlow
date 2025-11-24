package com.diom.flowable;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Flowable 工作流服务启动类
 *
 * @author diom
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
@MapperScan("com.diom.flowable.mapper")
public class FlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Flowable 工作流服务启动成功！");
        System.out.println("  端口: 8086");
        System.out.println("  数据库: diom_flowable");
        System.out.println("  引擎: Flowable 6.8.0");
        System.out.println("========================================\n");
    }
}

