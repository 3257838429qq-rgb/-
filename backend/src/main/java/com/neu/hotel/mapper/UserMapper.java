package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供用户基础的CRUD操作
 * - 定义按用户名查询的方法
 *
 * 【自定义查询方法】
 * 1. selectByUsername: 根据用户名查询用户（用于登录验证）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/UserMapper.xml
 *
 * 【关联关系】
 * - 对应实体：User
 * - 被UserService、AuthService调用
 *
 * 【对应前端】
 * - API：@/api/system/user.js, @/api/auth.js
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 用于登录验证和用户名唯一性检查
     * @param username 用户名
     * @return 用户信息（未找到返回null）
     */
    User selectByUsername(@Param("username") String username);
}
