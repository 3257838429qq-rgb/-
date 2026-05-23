package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    IPage<Visitor> selectVisitorPage(Page<?> page, @Param("params") Map<String, Object> params);

    IPage<Visitor> selectVisitorByStatus(Page<?> page, @Param("status") Integer status, @Param("params") Map<String, Object> params);
}
