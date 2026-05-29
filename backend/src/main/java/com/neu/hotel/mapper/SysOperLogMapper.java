package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供日志基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/SysOperLogMapper.xml
 *
 * 【关联关系】
 * - 对应实体：SysOperLog
 * - 被SysOperLogService调用
 * - 通过LogAspect自动记录操作日志
 *
 * 【对应前端】
 * - API：@/api/system/log.js
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
    // 继承BaseMapper即可满足基本CRUD需求
}
