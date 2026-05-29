package com.neu.hotel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.JwtUtil;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.Menu;
import com.neu.hotel.entity.Role;
import com.neu.hotel.entity.User;
import com.neu.hotel.mapper.MenuMapper;
import com.neu.hotel.mapper.RoleMapper;
import com.neu.hotel.mapper.UserMapper;
import com.neu.hotel.service.MenuService;
import com.neu.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理服务实现类
 *
 * 【功能说明】
 * - 处理用户登录验证
 * - 用户注册和密码管理
 * - 用户分页查询
 *
 * 【安全机制】
 * - 密码使用BCrypt加密存储
 * - 登录时验证加密后的密码
 * - 修改密码需验证原密码
 *
 * 【关联关系】
 * - 依赖 UserMapper 操作用户数据
 * - 依赖 RoleMapper 查询角色信息
 * - 依赖 MenuService 获取用户菜单权限
 * - 被 AuthController、UserController 调用
 *
 * 【对应前端】
 * - API：@/api/system/user.js, @/api/auth.js
 * - Vue组件：@/views/login/index.vue
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // BCrypt密码加密器，用于密码加密和验证
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 明文密码
     * @return 验证成功返回用户信息，失败返回null
     */
    @Override
    public User login(String username, String password) {
        User user = selectByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 用户注册
     * - 检查用户名是否已存在
     * - 自动分配"普通用户"角色
     * - 密码加密存储
     * @param user 用户信息
     */
    @Override
    public void register(User user) {
        User exist = selectByUsername(user.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 自动分配普通用户角色
        if (user.getRoleId() == null) {
            Long guestRoleId = getOrCreateGuestRole();
            user.setRoleId(guestRoleId);
        }
        // BCrypt加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.insert(user);
    }

    /**
     * 获取或创建普通用户角色
     * 用于新用户注册时自动分配角色
     * @return 普通用户角色ID
     */
    private Long getOrCreateGuestRole() {
        // 查找普通用户角色
        Role guestRole = roleMapper.selectByCode("GUEST");
        if (guestRole != null) {
            return guestRole.getId();
        }
        // 如果不存在，创建普通用户角色
        Role newRole = new Role();
        newRole.setName("普通用户");
        newRole.setCode("GUEST");
        newRole.setDescription("普通用户/访客");
        newRole.setStatus(1);
        roleMapper.insert(newRole);
        return newRole.getId();
    }

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public User getUserById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User selectByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 分页查询用户列表
     * 支持按用户名、真实姓名、角色、状态筛选
     * @param current 当前页
     * @param size 每页大小
     * @param user 查询条件
     * @return 用户分页结果
     */
    @Override
    public IPage<User> selectPage(Long current, Long size, User user) {
        Page<User> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(user.getUsername())) {
            wrapper.like(User::getUsername, user.getUsername());
        }
        if (StrUtil.isNotBlank(user.getRealName())) {
            wrapper.like(User::getRealName, user.getRealName());
        }
        if (user.getRoleId() != null) {
            wrapper.eq(User::getRoleId, user.getRoleId());
        }
        if (user.getStatus() != null) {
            wrapper.eq(User::getStatus, user.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 修改用户密码
     * - 验证原密码
     * - 加密新密码存储
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return baseMapper.updateById(user) > 0;
    }

    /**
     * 获取所有用户列表
     * 用于下拉框等场景
     * @return 用户列表
     */
    @Override
    public List<User> selectAll() {
        return baseMapper.selectList(null);
    }

    /**
     * 密码加密方法（供Controller调用）
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 密码验证方法（供Controller调用）
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
