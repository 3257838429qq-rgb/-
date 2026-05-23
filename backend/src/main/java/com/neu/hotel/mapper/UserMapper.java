package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@Param("username") String username);
}
