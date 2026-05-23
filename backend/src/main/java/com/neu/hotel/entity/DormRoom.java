package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_dorm_room")
public class DormRoom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomNo;

    private String roomName;

    private Long roomTypeId;

    private String floor;

    private Integer capacity;

    private Integer maxCapacity;

    private BigDecimal price;

    private Integer status;

    private String facility;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private BigDecimal priceMin;

    @TableField(exist = false)
    private BigDecimal priceMax;
}
