package com.diom.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author diom
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);
}

