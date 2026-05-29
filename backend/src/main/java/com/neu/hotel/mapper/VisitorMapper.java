package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

/**
 * 访客Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供访客基础的CRUD操作
 * - 定义访客的分页查询和条件查询方法
 *
 * 【自定义查询方法】
 * 1. selectVisitorPage: 分页查询访客列表
 * 2. selectVisitorByStatus: 按状态查询访客列表
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/VisitorMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Visitor
 * - 被VisitorService、CheckInService调用
 *
 * 【对应前端】
 * - API：@/api/visitor/index.js
 */
@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    /**
     * 分页查询访客列表
     * @param page MyBatis-Plus分页对象
     * @param params 查询条件（可包含：name, phone, status等）
     * @return 访客分页结果
     */
    IPage<Visitor> selectVisitorPage(Page<?> page, @Param("params") Map<String, Object> params);

    /**
     * 按状态查询访客列表
     * @param page MyBatis-Plus分页对象
     * @param status 访客状态（0=待审核, 1=已通过, 2=已拒绝）
     * @param params 其他查询条件
     * @return 指定状态的访客分页结果
     */
    IPage<Visitor> selectVisitorByStatus(Page<?> page, @Param("status") Integer status, @Param("params") Map<String, Object> params);
}
