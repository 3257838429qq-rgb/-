package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.Dept;
import com.neu.hotel.mapper.DeptMapper;
import com.neu.hotel.service.DeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现类
 *
 * 【功能说明】
 * - 提供部门数据的树形结构构建
 * - 支持多级部门组织架构
 *
 * 【关联关系】
 * - 依赖 DeptMapper 操作部门数据
 * - 被 DeptController 调用
 * - 被 Visitor 实体引用（通过 hostDeptId）
 *
 * 【对应前端】
 * - API：@/api/system/dept.js
 * - Vue组件：@/views/system/dept/index.vue
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    /**
     * 获取部门树形结构
     * 将平铺的部门数据构建成树形结构，便于前端Tree组件渲染
     * @return 树形部门列表
     */
    @Override
    public List<Dept> selectTree() {
        List<Dept> allDepts = baseMapper.selectList(null);
        return buildTree(allDepts, 0L);
    }

    /**
     * 递归构建部门树
     * @param depts 所有部门列表
     * @param parentId 父级部门ID
     * @return 子部门列表
     */
    private List<Dept> buildTree(List<Dept> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .collect(Collectors.toList());
    }
}
