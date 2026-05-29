package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.Role;
import com.neu.hotel.mapper.RoleMapper;
import com.neu.hotel.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色管理服务实现类
 *
 * 【功能说明】
 * - 提供角色数据的CRUD操作
 * - 简单实现，主要用于角色下拉框等场景
 *
 * 【关联关系】
 * - 依赖 RoleMapper 操作角色数据
 * - 被 RoleController 调用
 * - 被 User 实体引用（通过 roleId）
 *
 * 【对应前端】
 * - API：@/api/system/role.js
 * - Vue组件：@/views/system/role/index.vue
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @Override
    public List<Role> selectAll() {
        return baseMapper.selectList(null);
    }
}
