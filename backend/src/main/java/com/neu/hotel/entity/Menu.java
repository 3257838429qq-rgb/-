package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体类
 *
 * 【数据库表】sys_menu - 系统菜单表
 *
 * 【主要功能】
 * - 定义系统菜单结构（树形）
 * - 存储菜单的路由和组件信息
 * - 管理权限标识（perms）
 *
 * 【字段说明】
 * - id: 菜单唯一标识（自增主键）
 * - name: 菜单名称
 * - path: 路由路径
 * - component: 前端组件路径
 * - parentId: 父级菜单ID（构建树形结构）
 * - orderNum: 排序号
 * - icon: 菜单图标
 * - perms: 权限标识（如"user:add"）
 * - menuType: 菜单类型（1=目录，2=菜单，3=按钮）
 * - status: 状态（1=启用，0=停用）
 * - children: 子菜单列表（非数据库字段）
 *
 * 【关联关系】
 * - 自关联形成树形菜单结构
 * - 通过 Role 实体管理角色菜单权限
 * - 通过 MenuService 处理菜单业务逻辑
 * - 通过 MenuMapper 查询菜单数据
 *
 * 【对应前端】
 * - 路由：/admin/system/menu
 * - Vue组件：@/views/system/menu/index.vue
 */
@Data
@TableName("sys_menu")
public class Menu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 菜单唯一标识

    // 非数据库字段，用于存储子菜单列表，构建树形结构
    @TableField(exist = false)
    private List<Menu> children;

    private String name;          // 菜单名称

    private String path;          // 路由路径

    private String component;     // 前端组件路径

    private Long parentId;        // 父级菜单ID

    private Integer orderNum;     // 排序号

    private String icon;          // 菜单图标

    private String perms;        // 权限标识

    private Integer menuType;     // 菜单类型

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;      // 逻辑删除标记
}
