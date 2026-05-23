package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.Menu;

import java.util.List;

public interface MenuService extends IService<Menu> {

    List<Menu> selectMenusByRoleId(Long roleId);

    List<Menu> selectUserMenus(Long userId);

    List<Menu> selectMenuTree(Long roleId);
}
