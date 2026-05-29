package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.VipPackage;
import org.apache.ibatis.annotations.Mapper;

/**
 * VIP套餐Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供VIP套餐基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/VipPackageMapper.xml
 *
 * 【关联关系】
 * - 对应实体：VipPackage
 * - 被VipService调用
 *
 * 【对应前端】
 * - API：@/api/vip.js
 */
@Mapper
public interface VipPackageMapper extends BaseMapper<VipPackage> {
    // 继承BaseMapper即可满足基本CRUD需求
}
