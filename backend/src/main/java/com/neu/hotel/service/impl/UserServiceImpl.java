package com.neu.hotel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.JwtUtil;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.User;
import com.neu.hotel.mapper.UserMapper;
import com.neu.hotel.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        User user = selectByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public void register(User user) {
        User exist = selectByUsername(user.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.insert(user);
    }

    @Override
    public User getUserById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public User selectByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

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

    @Override
    public List<User> selectAll() {
        return baseMapper.selectList(null);
    }

    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
