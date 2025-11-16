package com.diom.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应
 *
 * @author diom
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Token
     */
    private String token;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo implements Serializable {
        private Long id;
        private String username;
        private String nickname;
        private String email;
    }
}

