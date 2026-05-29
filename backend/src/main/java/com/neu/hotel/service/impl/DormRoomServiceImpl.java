package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.mapper.DormRoomMapper;
import com.neu.hotel.service.DormRoomService;
import org.springframework.stereotype.Service;

/**
 * 房间管理服务实现类
 *
 * 【功能说明】
 * - 实现房间的CRUD操作
 * - 提供空闲房间查询
 * - 支持房间状态管理
 *
 * 【关联关系】
 * - 依赖 DormRoomMapper 操作房间数据
 * - 被 DormRoomController 调用
 * - 关联 DormRoomType 实体（通过 roomTypeId）
 *
 * 【对应前端】
 * - API：@/api/dorm/room.js
 * - Vue组件：@/views/dorm/room/index.vue
 */
@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {

    /**
     * 分页查询房间列表
     * 支持多条件筛选：房间号、名称、状态、楼层、房型、价格区间
     * @param current 当前页
     * @param size 每页大小
     * @param room 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<DormRoom> selectPage(Long current, Long size, DormRoom room) {
        Page<DormRoom> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        // 按房间号模糊查询
        if (room.getRoomNo() != null && !room.getRoomNo().isEmpty()) {
            wrapper.like(DormRoom::getRoomNo, room.getRoomNo());
        }
        // 按房间名称模糊查询
        if (room.getRoomName() != null && !room.getRoomName().isEmpty()) {
            wrapper.like(DormRoom::getRoomName, room.getRoomName());
        }
        // 按状态精确查询
        if (room.getStatus() != null) {
            wrapper.eq(DormRoom::getStatus, room.getStatus());
        }
        // 按楼层查询
        if (room.getFloor() != null && !room.getFloor().isEmpty()) {
            wrapper.eq(DormRoom::getFloor, room.getFloor());
        }
        // 按房型查询
        if (room.getRoomTypeId() != null) {
            wrapper.eq(DormRoom::getRoomTypeId, room.getRoomTypeId());
        }
        // 价格区间查询
        if (room.getPriceMin() != null) {
            wrapper.ge(DormRoom::getPrice, room.getPriceMin());
        }
        if (room.getPriceMax() != null) {
            wrapper.le(DormRoom::getPrice, room.getPriceMax());
        }
        wrapper.orderByDesc(DormRoom::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 查询空闲房间列表
     * 用于用户预订时选择可用房间
     * @param current 当前页
     * @param size 每页大小
     * @return 空闲房间分页结果
     */
    @Override
    public IPage<DormRoom> selectAvailableRooms(Long current, Long size) {
        Page<DormRoom> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DormRoom::getStatus, 1);  // 1=空闲
        wrapper.orderByAsc(DormRoom::getRoomNo);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 更新房间状态
     * 状态值：1=空闲, 2=入住, 3=维护, 4=停用
     * @param roomId 房间ID
     * @param status 新状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Long roomId, Integer status) {
        DormRoom room = baseMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        room.setStatus(status);
        return baseMapper.updateById(room) > 0;
    }
}
