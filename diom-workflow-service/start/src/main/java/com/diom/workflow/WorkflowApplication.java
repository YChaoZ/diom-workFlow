package com.diom.workflow;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 工作流服务启动类
 *
 * @author diom
 */
@SpringBootApplication(scanBasePackages = "com.diom.workflow")
@EnableDiscoveryClient
@EnableDubbo
@EnableProcessApplication
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
        System.out.println("==================================");
        System.out.println("====  Workflow 工作流服务启动成功！====");
        System.out.println("====  Dubbo Consumer 已启用       ====");
        System.out.println("==================================");
    }
}
