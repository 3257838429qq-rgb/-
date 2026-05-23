package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_check_in")
public class CheckIn implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long visitorId;

    private String idCard;

    private Long roomId;

    private String roomNo;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer checkInStatus;

    private Integer nights;

    private BigDecimal roomFee;

    private BigDecimal otherFee;

    private BigDecimal totalFee;

    private BigDecimal paidAmount;

    private Integer paymentStatus;

    private String paymentMethod;

    private String remark;

    private Long checkInUserId;

    private Long checkOutUserId;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String visitorName;

    @TableLogic
    private Integer deleted;
}
