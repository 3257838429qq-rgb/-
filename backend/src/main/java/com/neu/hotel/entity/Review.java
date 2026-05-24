package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_review")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkInId;

    private Long userId;

    private Long roomId;

    private Integer rating;

    private String content;

    private String reply;

    private LocalDateTime replyTime;

    private Long replyUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String roomNo;

    @TableField(exist = false)
    private String checkInDate;

    @TableField(exist = false)
    private String checkOutDate;

    @TableLogic
    private Integer deleted;
}
