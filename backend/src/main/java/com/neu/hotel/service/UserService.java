package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    void register(User user);

    User getUserById(Long id);

    User selectByUsername(String username);

    IPage<User> selectPage(Long current, Long size, User user);

    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    List<User> selectAll();
}
