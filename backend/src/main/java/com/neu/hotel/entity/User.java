package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 *
 * 【数据库表】sys_user - 系统用户表
 *
 * 【主要功能】
 * - 存储系统所有用户的基本信息
 * - 支持多种用户类型（管理员、普通员工等）
 *
 * 【字段说明】
 * - id: 用户唯一标识（自增主键）
 * - username: 用户登录账号
 * - password: 用户登录密码（加密存储）
 * - realName: 用户真实姓名
 * - phone: 联系电话
 * - email: 电子邮箱
 * - roleId: 关联的角色ID（对应 sys_role 表）
 * - status: 账号状态（1=正常，0=禁用）
 * - avatar: 用户头像URL
 * - userType: 用户类型（区分系统用户类型）
 * - createTime: 创建时间（插入时自动填充）
 * - updateTime: 更新时间（插入和更新时自动填充）
 * - deleted: 逻辑删除标记（0=未删除，1=已删除）
 *
 * 【关联关系】
 * - 通过 roleId 关联 Role 实体，获取用户角色信息
 * - 通过 UserService 操作用户数据
 * - 通过 UserController 提供用户管理API
 *
 * 【对应前端】
 * - 路由：/admin/system/user
 * - API：@/api/system/user.js
 * - Vue组件：@/views/system/user/index.vue
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;          // 用户唯一标识

    private String username;  // 用户登录账号

    private String password;  // 登录密码（加密）

    private String realName;  // 真实姓名

    private String phone;     // 联系电话

    private String email;    // 电子邮箱

    private Long roleId;      // 关联角色ID

    private Integer status;   // 账号状态

    private String avatar;    // 头像URL

    private Integer userType; // 用户类型

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;  // 逻辑删除标记
}
