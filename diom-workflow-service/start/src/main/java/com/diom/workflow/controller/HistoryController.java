package com.diom.workflow.controller;

import com.diom.common.dto.Result;
import com.diom.workflow.service.HistoryAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 历史参数分析 REST API
 *
 * @author diom
 */
@Slf4j
@RestController
@RequestMapping("/workflow/history")
public class HistoryController {

    @Autowired
    private HistoryAnalysisService historyAnalysisService;

    /**
     * 获取用户在指定流程中的常用参数
     */
    @GetMapping("/frequent-params")
    public Result<Map<String, Object>> getFrequentParams(
            @RequestParam String username,
            @RequestParam String processKey) {
        try {
            Map<String, Object> params = historyAnalysisService.getFrequentParams(username, processKey);
            return Result.success("获取成功", params);
        } catch (Exception e) {
            log.error("获取常用参数失败: username={}, processKey={}", username, processKey, e);
            return Result.fail("获取常用参数失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近一次流程的参数
     */
    @GetMapping("/last-params")
    public Result<Map<String, Object>> getLastParams(
            @RequestParam String username,
            @RequestParam String processKey) {
        try {
            Map<String, Object> params = historyAnalysisService.getLastParams(username, processKey);
            return Result.success("获取成功", params);
        } catch (Exception e) {
            log.error("获取最近参数失败: username={}, processKey={}", username, processKey, e);
            return Result.fail("获取最近参数失败: " + e.getMessage());
        }
    }
}

