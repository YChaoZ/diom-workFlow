package com.diom.web.adapter.controller;

import com.diom.common.constant.CommonConstant;
import com.diom.common.dto.Result;
import com.diom.web.adapter.dto.UserDTO;
import com.diom.web.app.service.UserAppService;
import com.diom.web.domain.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 Controller
 * Adapter 层，接收 HTTP 请求，转换为业务调用
 *
 * @author diom
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserAppService userAppService;

    /**
     * 获取用户信息（从网关传递的 Header 中获取当前登录用户 ID）
     *
     * @param userId Header 中的用户 ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestHeader(value = CommonConstant.USER_ID_HEADER, required = false) String userId) {
        log.info("获取用户信息，userId from header: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            return Result.fail("用户未登录");
        }
        
        try {
            Long userIdLong = Long.parseLong(userId);
            UserInfo userInfo = userAppService.getUserInfo(userIdLong);
            UserDTO userDTO = convertToDTO(userInfo);
            return Result.success(userDTO);
        } catch (NumberFormatException e) {
            return Result.fail("用户ID格式错误");
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户 ID 获取用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        log.info("根据ID获取用户信息，id: {}", id);
        
        try {
            UserInfo userInfo = userAppService.getUserInfo(id);
            UserDTO userDTO = convertToDTO(userInfo);
            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名获取用户信息，username: {}", username);
        
        try {
            UserInfo userInfo = userAppService.getUserByUsername(username);
            UserDTO userDTO = convertToDTO(userInfo);
            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 转换为 DTO
     */
    private UserDTO convertToDTO(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(userInfo, dto);
        dto.setStatusDesc(userInfo.getStatus() == 1 ? "正常" : "禁用");
        return dto;
    }
}

