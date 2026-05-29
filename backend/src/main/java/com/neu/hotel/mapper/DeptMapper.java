package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供部门基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper构建树形结构）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/DeptMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Dept
 * - 被DeptService调用
 *
 * 【对应前端】
 * - API：@/api/system/dept.js
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
    // 继承BaseMapper即可满足基本CRUD需求
}
