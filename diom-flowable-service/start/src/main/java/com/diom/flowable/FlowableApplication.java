package com.diom.flowable;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.flowable.spring.boot.app.AppEngineAutoConfiguration;
import org.flowable.spring.boot.app.AppEngineServicesAutoConfiguration;
import org.flowable.spring.boot.eventregistry.EventRegistryAutoConfiguration;
import org.flowable.spring.boot.eventregistry.EventRegistryServicesAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Flowable 工作流服务启动类
 * 
 * ⚠️ 重要：必须排除 App Engine 和 Event Registry 自动配置
 * 原因：这些引擎的 Liquibase 与手动导入的数据库脚本冲突
 * 结果：只保留核心 Process Engine（足够工作流功能使用）
 *
 * @author diom
 */
@SpringBootApplication(exclude = {
    AppEngineAutoConfiguration.class,
    AppEngineServicesAutoConfiguration.class,
    EventRegistryAutoConfiguration.class,
    EventRegistryServicesAutoConfiguration.class
})
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
        System.out.println("  引擎: Flowable 6.7.2 (Process Engine Only)");
        System.out.println("========================================\n");
    }
}

