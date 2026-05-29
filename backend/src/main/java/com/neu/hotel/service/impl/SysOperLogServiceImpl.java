package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.SysOperLog;
import com.neu.hotel.mapper.SysOperLogMapper;
import com.neu.hotel.service.SysOperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志服务实现类
 *
 * 【功能说明】
 * - 提供操作日志的分页查询
 * - 支持按标题、操作类型、状态、操作人筛选
 *
 * 【日志来源】
 * - 通过 @Log 注解在 Controller 层自动记录
 * - 记录内容包括：操作人、操作时间、操作类型、操作描述、IP地址等
 *
 * 【关联关系】
 * - 依赖 SysOperLogMapper 操作日志数据
 * - 被 SysOperLogController 调用
 *
 * 【对应前端】
 * - API：@/api/system/log.js
 * - Vue组件：@/views/system/log/index.vue
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    /**
     * 分页查询操作日志
     * 支持多条件筛选
     * @param current 当前页
     * @param size 每页大小
     * @param log 查询条件
     * @return 日志分页结果
     */
    @Override
    public IPage<SysOperLog> selectPage(Long current, Long size, SysOperLog log) {
        Page<SysOperLog> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (log != null) {
            // 按标题模糊查询
            if (StringUtils.isNotBlank(log.getTitle())) {
                wrapper.like(SysOperLog::getTitle, log.getTitle());
            }
            // 按操作类型精确查询
            if (StringUtils.isNotBlank(log.getOperType())) {
                wrapper.eq(SysOperLog::getOperType, log.getOperType());
            }
            // 按状态查询
            if (log.getStatus() != null) {
                wrapper.eq(SysOperLog::getStatus, log.getStatus());
            }
            // 按操作人查询
            if (log.getUserId() != null) {
                wrapper.eq(SysOperLog::getUserId, log.getUserId());
            }
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
