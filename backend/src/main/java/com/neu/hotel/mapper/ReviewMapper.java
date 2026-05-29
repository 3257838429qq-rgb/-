package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供评价基础的CRUD操作
 * - 无需自定义复杂查询方法
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/ReviewMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Review
 * - 被ReviewService调用
 *
 * 【对应前端】
 * - API：@/api/review.js
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    // 继承BaseMapper即可满足基本CRUD需求
    // 如需复杂查询，可在对应的XML映射文件中添加
}
