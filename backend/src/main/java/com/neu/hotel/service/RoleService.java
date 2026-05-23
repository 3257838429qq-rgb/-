package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> selectAll();
}
