package com.diom.workflow.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Camunda 配置类 - 7.16版本（已修复MySQL 8兼容性）
 */
@Configuration
public class CamundaConfig {

    private static final Logger log = LoggerFactory.getLogger(CamundaConfig.class);

    @Bean
    @Order(Ordering.DEFAULT_ORDER + 1)
    public ProcessEnginePlugin camundaCustomPlugin() {
        return new AbstractProcessEnginePlugin() {
            @Override
            public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
                log.info("正在配置Camunda 7.16...");
                
                // Camunda 7.16已修复MySQL 8.0兼容性问题，无需特殊配置
                // 只保留基本的优化配置
                
                // 启用JDBC批处理（性能优化）
                processEngineConfiguration.setJdbcBatchProcessing(true);
                
                log.info("Camunda 7.16配置完成 - MySQL 8.0原生支持");
            }
        };
    }
}

