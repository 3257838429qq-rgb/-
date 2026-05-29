package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 角色Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供角色基础的CRUD操作
 * - 定义按角色编码查询的方法
 *
 * 【自定义查询方法】
 * 1. selectByCode: 根据角色编码查询角色（用于查找普通用户角色）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/RoleMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Role
 * - 被RoleService、UserService调用
 *
 * 【对应前端】
 * - API：@/api/system/role.js
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     * 用于查找特定角色（如GUEST普通用户角色）
     * @param code 角色编码
     * @return 角色信息（未找到返回null）
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code} AND deleted = 0 LIMIT 1")
    Role selectByCode(String code);
}
