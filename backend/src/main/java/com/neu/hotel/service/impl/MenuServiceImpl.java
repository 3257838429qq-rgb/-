package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.Menu;
import com.neu.hotel.mapper.MenuMapper;
import com.neu.hotel.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<Menu> selectMenusByRoleId(Long roleId) {
        return baseMapper.selectMenusByRoleId(roleId);
    }

    @Override
    public List<Menu> selectUserMenus(Long userId) {
        return baseMapper.selectUserMenus(userId);
    }

    @Override
    public List<Menu> selectMenuTree(Long roleId) {
        List<Menu> allMenus = baseMapper.selectMenusByRoleId(roleId);
        return buildTree(allMenus, 0L);
    }

    private List<Menu> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(buildTree(menus, menu.getId())))
                .collect(Collectors.toList());
    }
}
