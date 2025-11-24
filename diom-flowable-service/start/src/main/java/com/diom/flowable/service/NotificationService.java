package com.diom.flowable.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.diom.flowable.entity.WorkflowNotification;
import com.diom.flowable.mapper.WorkflowNotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private WorkflowNotificationMapper notificationMapper;

    /**
     * 创建通知
     */
    @Transactional
    public void createNotification(String username, String type, String title, 
                                   String content, String linkType, String linkId, String priority) {
        WorkflowNotification notification = new WorkflowNotification();
        notification.setUsername(username);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLinkType(linkType);
        notification.setLinkId(linkId);
        notification.setIsRead(0);
        notification.setPriority(priority != null ? priority : "NORMAL");
        
        notificationMapper.insert(notification);
        log.info("创建通知: username={}, type={}, title={}", username, type, title);
    }

    /**
     * 获取用户通知列表
     */
    public List<WorkflowNotification> getUserNotifications(String username, Boolean unreadOnly) {
        QueryWrapper<WorkflowNotification> query = new QueryWrapper<>();
        query.eq("username", username);
        if (unreadOnly != null && unreadOnly) {
            query.eq("is_read", 0);
        }
        query.orderByDesc("create_time");
        return notificationMapper.selectList(query);
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount(String username) {
        QueryWrapper<WorkflowNotification> query = new QueryWrapper<>();
        query.eq("username", username);
        query.eq("is_read", 0);
        return notificationMapper.selectCount(query);
    }

    /**
     * 标记为已读
     */
    @Transactional
    public void markAsRead(Long id) {
        WorkflowNotification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
            log.info("标记通知为已读: id={}", id);
        }
    }

    /**
     * 全部标记为已读
     */
    @Transactional
    public void markAllAsRead(String username) {
        QueryWrapper<WorkflowNotification> query = new QueryWrapper<>();
        query.eq("username", username);
        query.eq("is_read", 0);
        
        List<WorkflowNotification> notifications = notificationMapper.selectList(query);
        for (WorkflowNotification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        log.info("全部标记为已读: username={}, count={}", username, notifications.size());
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long id) {
        notificationMapper.deleteById(id);
        log.info("删除通知: id={}", id);
    }
}

