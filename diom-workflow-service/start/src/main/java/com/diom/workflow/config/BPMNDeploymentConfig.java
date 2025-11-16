package com.diom.workflow.config;

import org.camunda.bpm.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * BPMN流程手动部署配置
 * 解决自动部署不生效的问题
 */
@Configuration
public class BPMNDeploymentConfig {

    private static final Logger log = LoggerFactory.getLogger(BPMNDeploymentConfig.class);

    @Bean
    public CommandLineRunner deployBPMNProcesses(RepositoryService repositoryService) {
        return args -> {
            try {
                log.info("开始手动部署BPMN流程定义...");
                
                // 扫描classpath下的所有BPMN文件
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath*:processes/*.bpmn");
                
                log.info("找到 {} 个BPMN文件", resources.length);
                
                for (Resource resource : resources) {
                    String filename = resource.getFilename();
                    log.info("正在部署流程文件: {}", filename);
                    
                    try {
                        repositoryService.createDeployment()
                                .name(filename)
                                .addInputStream(filename, resource.getInputStream())
                                .enableDuplicateFiltering(false) // 允许重复部署
                                .deploy();
                        
                        log.info("✅ 流程文件部署成功: {}", filename);
                    } catch (Exception e) {
                        log.error("❌ 流程文件部署失败: {}, 错误: {}", filename, e.getMessage(), e);
                    }
                }
                
                // 输出所有已部署的流程定义
                long count = repositoryService.createProcessDefinitionQuery().count();
                log.info("当前共有 {} 个流程定义", count);
                
                repositoryService.createProcessDefinitionQuery().list().forEach(def -> {
                    log.info("流程定义: key={}, name={}, version={}", 
                            def.getKey(), def.getName(), def.getVersion());
                });
                
            } catch (Exception e) {
                log.error("BPMN流程部署失败", e);
            }
        };
    }
}

