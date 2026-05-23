package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);

    List<Menu> selectUserMenus(@Param("userId") Long userId);
}
