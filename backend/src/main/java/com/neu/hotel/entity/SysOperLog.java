package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String businessType;

    private String operType;

    private String method;

    private String requestMethod;

    private Long userId;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String result;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private LocalDateTime operTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
