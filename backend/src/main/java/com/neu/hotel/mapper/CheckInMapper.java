package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    IPage<CheckIn> selectCheckInPage(Page<?> page, @Param("params") Map<String, Object> params);

    IPage<CheckIn> selectActiveCheckIns(Page<?> page);

    IPage<CheckIn> selectCheckInByDateRange(Page<?> page, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
