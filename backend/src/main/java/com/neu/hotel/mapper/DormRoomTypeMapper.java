package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.DormRoomType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间类型Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供房间类型基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/DormRoomTypeMapper.xml
 *
 * 【关联关系】
 * - 对应实体：DormRoomType
 * - 被DormRoomTypeService、DormRoomService调用
 *
 * 【对应前端】
 * - API：@/api/dorm/roomType.js
 */
@Mapper
public interface DormRoomTypeMapper extends BaseMapper<DormRoomType> {
    // 继承BaseMapper即可满足基本CRUD需求
}
