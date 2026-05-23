package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.Dept;

import java.util.List;

public interface DeptService extends IService<Dept> {

    List<Dept> selectTree();
}
