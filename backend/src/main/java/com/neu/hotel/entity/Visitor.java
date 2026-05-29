package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访客实体类
 *
 * 【数据库表】biz_visitor - 访客表
 *
 * 【主要功能】
 * - 记录访客的基本信息
 * - 管理访客预约登记
 * - 记录来访目的和接待信息
 *
 * 【字段说明】
 * - id: 访客唯一标识（自增主键）
 * - name: 访客姓名
 * - phone: 访客联系电话
 * - idCard: 访客身份证号
 * - company: 访客所属单位/公司
 * - visitPurpose: 来访目的
 * - visitDate: 计划来访日期
 * - visitTimeSlot: 来访时间段
 * - hostDeptId: 接待部门ID（对应 biz_dorm_room 表）
 * - hostPerson: 接待人姓名
 * - hostPersonPhone: 接待人电话
 * - carPlate: 车牌号（如有车辆）
 * - remark: 备注信息
 * - status: 状态（1=待审核，2=已通过，3=已拒绝，4=已到访，5=已离开）
 * - reviewerId: 审核人ID
 * - reviewRemark: 审核备注
 * - reviewTime: 审核时间
 * - createUserId: 创建人ID
 *
 * 【关联关系】
 * - 通过 hostDeptId 关联 Dept 实体
 * - 通过 CheckIn 实体可查看该访客的入住记录
 * - 通过 VisitorService 处理访客业务逻辑
 * - 通过 VisitorController 提供访客管理API
 *
 * 【对应前端】
 * - 路由：/admin/visitor/list
 * - API：@/api/visitor/index.js
 * - Vue组件：@/views/visitor/list/index.vue
 */
@Data
@TableName("biz_visitor")
public class Visitor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 访客唯一标识

    private String name;          // 访客姓名

    private String phone;         // 联系电话

    private String idCard;         // 身份证号

    private String company;        // 所属单位

    private String visitPurpose;    // 来访目的

    private LocalDate visitDate;   // 计划来访日期

    private String visitTimeSlot;  // 来访时间段

    private Long hostDeptId;       // 接待部门ID

    private String hostPerson;     // 接待人姓名

    private String hostPersonPhone; // 接待人电话

    private String carPlate;       // 车牌号

    private String remark;         // 备注

    private Integer status;        // 状态

    private Long reviewerId;       // 审核人ID

    private String reviewRemark;   // 审核备注

    private LocalDateTime reviewTime; // 审核时间

    private Long createUserId;     // 创建人ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;        // 逻辑删除标记
}
