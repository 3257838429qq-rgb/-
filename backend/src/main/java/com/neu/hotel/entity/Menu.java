package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class Menu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(exist = false)
    private List<Menu> children;

    private String name;

    private String path;

    private String component;

    private Long parentId;

    private Integer orderNum;

    private String icon;

    private String perms;

    private Integer menuType;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
