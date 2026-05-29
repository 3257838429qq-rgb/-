package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 *
 * 【数据库表】sys_oper_log - 系统操作日志表
 *
 * 【主要功能】
 * - 记录用户在系统中的所有操作
 * - 用于审计追踪和问题排查
 * - 监控接口调用和性能
 *
 * 【字段说明】
 * - id: 日志唯一标识（自增主键）
 * - title: 操作标题（如"新增用户"、"删除房间"）
 * - businessType: 业务类型
 * - operType: 操作类型（1=新增，2=修改，3=删除，4=查询等）
 * - method: 请求的方法名
 * - requestMethod: HTTP请求方法（GET/POST/PUT/DELETE）
 * - userId: 操作人ID
 * - operUrl: 请求URL
 * - operIp: 操作人IP地址
 * - operParam: 请求参数
 * - result: 返回结果
 * - status: 操作状态（1=成功，0=失败）
 * - errorMsg: 错误信息
 * - costTime: 请求耗时（毫秒）
 * - operTime: 操作时间
 *
 * 【关联关系】
 * - 通过 @Log 注解自动记录操作
 * - 通过 LogAspect 切面类拦截并记录日志
 * - 通过 SysOperLogService 处理日志业务逻辑
 * - 通过 SysOperLogController 提供日志查询API
 *
 * 【对应前端】
 * - 路由：/admin/system/log
 * - API：@/api/system/log.js
 * - Vue组件：@/views/system/log/index.vue
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 日志唯一标识

    private String title;        // 操作标题

    private String businessType;  // 业务类型

    private String operType;     // 操作类型

    private String method;       // 请求方法名

    private String requestMethod;  // HTTP请求方法

    private Long userId;         // 操作人ID

    private String operUrl;      // 请求URL

    private String operIp;       // 操作人IP

    private String operParam;    // 请求参数

    private String result;       // 返回结果

    private Integer status;      // 操作状态

    private String errorMsg;      // 错误信息

    private Long costTime;       // 请求耗时

    private LocalDateTime operTime;  // 操作时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableLogic
    private Integer deleted;       // 逻辑删除标记
}
