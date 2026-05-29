package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.VipRecharge;
import org.apache.ibatis.annotations.Mapper;

/**
 * VIP充值记录Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供充值记录基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/VipRechargeMapper.xml
 *
 * 【关联关系】
 * - 对应实体：VipRecharge
 * - 被VipService调用
 *
 * 【对应前端】
 * - API：@/api/vip.js
 */
@Mapper
public interface VipRechargeMapper extends BaseMapper<VipRecharge> {
    // 继承BaseMapper即可满足基本CRUD需求
}
