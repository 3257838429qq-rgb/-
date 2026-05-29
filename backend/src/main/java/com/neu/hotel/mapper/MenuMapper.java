package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 菜单Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供菜单基础的CRUD操作
 * - 定义按角色ID查询菜单的方法
 *
 * 【自定义查询方法】
 * 1. selectMenusByRoleId: 根据角色ID查询菜单列表
 * 2. selectUserMenus: 根据用户ID查询菜单列表
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/MenuMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Menu
 * - 被MenuService调用
 * - 登录时返回给前端用于渲染菜单
 *
 * 【对应前端】
 * - API：@/api/auth.js（登录时返回）
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据角色ID查询菜单列表
     * 用于登录后获取该角色的菜单权限
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询菜单列表
     * 可以直接按用户授权查询菜单
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> selectUserMenus(@Param("userId") Long userId);
}
