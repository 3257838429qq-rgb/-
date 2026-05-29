package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

/**
 * 入住记录Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供基础的CRUD操作
 * - 定义入住记录的分页查询和条件查询方法
 *
 * 【自定义查询方法】
 * 1. selectCheckInPage: 分页查询入住记录（支持多条件筛选）
 * 2. selectActiveCheckIns: 查询当前入住中的记录
 * 3. selectPendingCheckIns: 查询待审批的记录
 * 4. selectMyBookings: 查询指定用户的预订记录
 * 5. selectCheckInByDateRange: 按日期范围查询
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/CheckInMapper.xml
 *
 * 【关联关系】
 * - 对应实体：CheckIn
 * - 被Service层调用
 *
 * 【对应前端】
 * - API：@/api/dorm/checkin.js
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 分页查询入住记录
     * @param page MyBatis-Plus分页对象
     * @param params 查询条件（可包含：visitorName, roomNo, status, checkInStatus, paymentStatus等）
     * @return 分页结果
     */
    IPage<CheckIn> selectCheckInPage(Page<?> page, @Param("params") Map<String, Object> params);

    /**
     * 查询当前入住中的记录（已入住 + 退房申请中）
     * @param page MyBatis-Plus分页对象
     * @return 入住中的记录分页结果
     */
    IPage<CheckIn> selectActiveCheckIns(Page<?> page);

    /**
     * 查询待审批的入住记录
     * @param page MyBatis-Plus分页对象
     * @param status 入住状态（0=待审核）
     * @return 待审批记录分页结果
     */
    IPage<CheckIn> selectPendingCheckIns(Page<?> page, @Param("status") Integer status);

    /**
     * 查询指定用户的预订记录
     * @param page MyBatis-Plus分页对象
     * @param userId 用户ID
     * @return 用户预订记录分页结果
     */
    IPage<CheckIn> selectMyBookings(Page<?> page, @Param("userId") Long userId);

    /**
     * 按日期范围查询入住记录
     * @param page MyBatis-Plus分页对象
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的记录
     */
    IPage<CheckIn> selectCheckInByDateRange(Page<?> page, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
