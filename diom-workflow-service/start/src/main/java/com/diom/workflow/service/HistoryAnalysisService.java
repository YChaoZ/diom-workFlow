package com.diom.workflow.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史参数分析服务
 * 从用户的历史流程实例中提取常用参数
 *
 * @author diom
 */
@Slf4j
@Service
public class HistoryAnalysisService {

    @Autowired
    private HistoryService historyService;

    /**
     * 获取用户在指定流程中的常用参数
     * 分析用户最近的流程实例，统计最频繁使用的参数值
     */
    public Map<String, Object> getFrequentParams(String username, String processKey) {
        log.info("分析用户常用参数: username={}, processKey={}", username, processKey);

        // 获取用户最近的历史流程实例（最多10个）
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .variableValueEquals("applicant", username)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .listPage(0, 10);

        if (instances.isEmpty()) {
            log.info("用户没有历史流程实例");
            return Collections.emptyMap();
        }

        // 统计各参数的频率
        Map<String, Map<Object, Integer>> paramFrequency = new HashMap<>();

        for (HistoricProcessInstance instance : instances) {
            Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(
                            v -> v.getName(),
                            v -> v.getValue(),
                            (v1, v2) -> v1
                    ));

            // 统计每个参数值的出现次数
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();

                if (paramValue != null && !paramName.equals("applicant")) {
                    paramFrequency
                            .computeIfAbsent(paramName, k -> new HashMap<>())
                            .merge(paramValue, 1, Integer::sum);
                }
            }
        }

        // 提取每个参数最常用的值
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Map<Object, Integer>> entry : paramFrequency.entrySet()) {
            String paramName = entry.getKey();
            Map<Object, Integer> valueFreq = entry.getValue();

            // 找出出现次数最多的值
            Object mostFrequentValue = valueFreq.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (mostFrequentValue != null) {
                result.put(paramName, mostFrequentValue);
            }
        }

        log.info("分析结果: {}", result);
        return result;
    }

    /**
     * 获取用户最近一次流程的参数
     */
    public Map<String, Object> getLastParams(String username, String processKey) {
        log.info("获取用户最近参数: username={}, processKey={}", username, processKey);

        // 获取最近的一个流程实例
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .variableValueEquals("applicant", username)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .listPage(0, 1)
                .stream()
                .findFirst()
                .orElse(null);

        if (instance == null) {
            log.info("用户没有历史流程实例");
            return Collections.emptyMap();
        }

        // 获取该流程实例的所有变量
        Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getId())
                .list()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getName(),
                        v -> v.getValue(),
                        (v1, v2) -> v1
                ));

        // 移除applicant参数（不需要回填）
        variables.remove("applicant");

        log.info("最近参数: {}", variables);
        return variables;
    }
}

