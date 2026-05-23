package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_visitor")
public class Visitor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    private String idCard;

    private String company;

    private String visitPurpose;

    private LocalDate visitDate;

    private String visitTimeSlot;

    private Long hostDeptId;

    private String hostPerson;

    private String hostPersonPhone;

    private String carPlate;

    private String remark;

    private Integer status;

    private Long reviewerId;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
