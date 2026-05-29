package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体类
 *
 * 【数据库表】sys_dept - 系统部门表
 *
 * 【主要功能】
 * - 管理酒店内部组织架构
 * - 记录部门信息及负责人
 * - 支持来访接待的部门分配
 *
 * 【字段说明】
 * - id: 部门唯一标识（自增主键）
 * - name: 部门名称
 * - parentId: 父级部门ID（支持多级部门）
 * - code: 部门编码
 * - leader: 部门负责人姓名
 * - phone: 负责人联系电话
 * - email: 负责人邮箱
 * - orderNum: 排序号
 * - status: 状态（1=启用，0=停用）
 *
 * 【关联关系】
 * - 自关联形成树形部门结构
 * - 被 Visitor 实体通过 hostDeptId 引用
 * - 通过 DeptService 处理部门业务逻辑
 * - 通过 DeptController 提供部门管理API
 *
 * 【对应前端】
 * - 路由：/admin/system/dept
 * - API：@/api/system/dept.js
 * - Vue组件：@/views/system/dept/index.vue
 */
@Data
@TableName("sys_dept")
public class Dept implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 部门唯一标识

    private String name;          // 部门名称

    private Long parentId;        // 父级部门ID

    private String code;          // 部门编码

    private String leader;        // 部门负责人

    private String phone;         // 联系电话

    private String email;         // 邮箱

    private Integer orderNum;     // 排序号

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;      // 逻辑删除标记
}
