package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 *
 * 【数据库表】sys_role - 系统角色表
 *
 * 【主要功能】
 * - 定义系统中的角色（如管理员、前台、客服等）
 * - 管理角色的权限标识
 * - 控制用户的访问权限
 *
 * 【字段说明】
 * - id: 角色唯一标识（自增主键）
 * - name: 角色名称
 * - code: 角色代码（唯一标识，如"ADMIN"）
 * - description: 角色描述
 * - status: 状态（1=启用，0=停用）
 *
 * 【关联关系】
 * - 通过 Menu 实体管理角色菜单权限
 * - 被 User 实体通过 roleId 引用
 * - 通过 RoleService 处理角色业务逻辑
 * - 通过 RoleController 提供角色管理API
 *
 * 【对应前端】
 * - 路由：/admin/system/role
 * - API：@/api/system/role.js
 * - Vue组件：@/views/system/role/index.vue
 */
@Data
@TableName("sys_role")
public class Role implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 角色唯一标识

    private String name;          // 角色名称

    private String code;          // 角色代码

    private String description;   // 角色描述

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;      // 逻辑删除标记
}
