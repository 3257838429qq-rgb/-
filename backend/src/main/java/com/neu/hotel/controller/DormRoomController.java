package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.entity.DormRoomType;
import com.neu.hotel.service.DormRoomService;
import com.neu.hotel.service.DormRoomTypeService;
import org.springframework.web.bind.annotation.*;

/**
 * 房间管理控制器
 *
 * 【功能说明】
 * - 管理酒店房间信息
 * - 查询空闲房间
 * - 管理房间类型
 *
 * 【API前缀】/dorm/room
 *
 * 【主要接口】
 * 1. 分页查询房间 - GET /dorm/room/page
 * 2. 查询空闲房间 - GET /dorm/room/available
 * 3. 获取房间类型 - GET /dorm/room/types
 * 4. 获取房间详情 - GET /dorm/room/{id}
 * 5. 新增房间 - POST /dorm/room
 * 6. 修改房间 - PUT /dorm/room
 * 7. 修改房间状态 - PUT /dorm/room/status/{id}
 * 8. 删除房间 - DELETE /dorm/room/{id}
 *
 * 【关联关系】
 * - 依赖 DormRoomService 处理房间业务
 * - 依赖 DormRoomTypeService 处理房间类型
 * - 关联 DormRoomType 实体（通过 roomTypeId）
 *
 * 【对应前端】
 * - 路由：/admin/dorm/room
 * - API：@/api/dorm/room.js
 * - Vue组件：@/views/dorm/room/index.vue
 */
@RestController
@RequestMapping("/dorm/room")
public class DormRoomController {

    private final DormRoomService dormRoomService;
    private final DormRoomTypeService dormRoomTypeService;

    public DormRoomController(DormRoomService dormRoomService, DormRoomTypeService dormRoomTypeService) {
        this.dormRoomService = dormRoomService;
        this.dormRoomTypeService = dormRoomTypeService;
    }

    /**
     * 分页查询房间列表
     * GET /dorm/room/page?current=1&size=10&roomNo=301&status=1
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          DormRoom room) {
        return Result.success(dormRoomService.selectPage(current, size, room));
    }

    /**
     * 查询空闲房间（供用户预订时选择）
     * GET /dorm/room/available?current=1&size=10
     */
    @GetMapping("/available")
    public Result getAvailable(@RequestParam(defaultValue = "1") Long current,
                               @RequestParam(defaultValue = "10") Long size) {
        return Result.success(dormRoomService.selectAvailableRooms(current, size));
    }

    /**
     * 获取所有房间类型（用于下拉框）
     * GET /dorm/room/types
     */
    @GetMapping("/types")
    public Result getRoomTypes() {
        return Result.success(dormRoomTypeService.selectAll());
    }

    /**
     * 根据ID获取房间详情
     * GET /dorm/room/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(dormRoomService.getById(id));
    }

    /**
     * 新增房间
     * POST /dorm/room
     */
    @PostMapping
    @Log(title = "房间管理", businessType = "INSERT", operType = "新增")
    public Result add(@RequestBody DormRoom room) {
        return Result.success(dormRoomService.save(room));
    }

    /**
     * 修改房间信息
     * PUT /dorm/room
     */
    @PutMapping
    @Log(title = "房间管理", businessType = "UPDATE", operType = "修改")
    public Result update(@RequestBody DormRoom room) {
        return Result.success(dormRoomService.updateById(room));
    }

    /**
     * 修改房间状态
     * PUT /dorm/room/status/{id}?status=1
     * status: 1=空闲, 2=入住, 3=维护, 4=停用
     */
    @PutMapping("/status/{id}")
    @Log(title = "房间管理", businessType = "UPDATE", operType = "修改状态")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(dormRoomService.updateStatus(id, status));
    }

    /**
     * 删除房间
     * DELETE /dorm/room/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "房间管理", businessType = "DELETE", operType = "删除")
    public Result delete(@PathVariable Long id) {
        return Result.success(dormRoomService.removeById(id));
    }
}
