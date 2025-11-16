package com.diom.workflow.controller;

import com.diom.common.dto.Result;
import com.diom.workflow.entity.WorkflowNotification;
import com.diom.workflow.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知 REST API
 */
@Slf4j
@RestController
@RequestMapping("/workflow/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping
    public Result<List<WorkflowNotification>> getNotifications(
            @RequestParam String username,
            @RequestParam(required = false) Boolean unreadOnly) {
        try {
            List<WorkflowNotification> notifications = 
                notificationService.getUserNotifications(username, unreadOnly);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.fail("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam String username) {
        try {
            long count = notificationService.getUnreadCount(username);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读数量失败", e);
            return Result.fail("获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 标记为已读
     */
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return Result.success("标记成功");
        } catch (Exception e) {
            log.error("标记为已读失败", e);
            return Result.fail("标记为已读失败: " + e.getMessage());
        }
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@RequestParam String username) {
        try {
            notificationService.markAllAsRead(username);
            return Result.success("全部标记成功");
        } catch (Exception e) {
            log.error("全部标记为已读失败", e);
            return Result.fail("全部标记为已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.fail("删除通知失败: " + e.getMessage());
        }
    }
}

