package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.DormRoomType;
import com.neu.hotel.service.DormRoomTypeService;
import org.springframework.web.bind.annotation.*;

/**
 * 房间类型管理控制器
 *
 * 【功能说明】
 * - 管理酒店房间类型（如单人间、标准间、套房等）
 * - 设置房型的价格和设施配置
 *
 * 【API前缀】/dorm/room-type
 *
 * 【主要接口】
 * 1. 获取房间类型列表 - GET /dorm/room-type/list
 * 2. 获取房间类型详情 - GET /dorm/room-type/{id}
 * 3. 新增房间类型 - POST /dorm/room-type
 * 4. 修改房间类型 - PUT /dorm/room-type
 * 5. 删除房间类型 - DELETE /dorm/room-type/{id}
 *
 * 【关联关系】
 * - 依赖 DormRoomTypeService 处理房型业务
 * - 被 DormRoomController 调用获取房间类型
 *
 * 【对应前端】
 * - 路由：/admin/dorm/room-type
 * - API：@/api/dorm/roomType.js
 * - Vue组件：@/views/dorm/room-type/index.vue
 */
@RestController
@RequestMapping("/dorm/room-type")
public class DormRoomTypeController {

    private final DormRoomTypeService dormRoomTypeService;

    public DormRoomTypeController(DormRoomTypeService dormRoomTypeService) {
        this.dormRoomTypeService = dormRoomTypeService;
    }

    /**
     * 获取所有房间类型列表
     * GET /dorm/room-type/list
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success(dormRoomTypeService.selectAll());
    }

    /**
     * 根据ID获取房间类型详情
     * GET /dorm/room-type/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(dormRoomTypeService.getById(id));
    }

    /**
     * 新增房间类型
     * POST /dorm/room-type
     */
    @PostMapping
    @Log(title = "房型管理", businessType = "INSERT", operType = "新增")
    public Result add(@RequestBody DormRoomType roomType) {
        return Result.success(dormRoomTypeService.save(roomType));
    }

    /**
     * 修改房间类型
     * PUT /dorm/room-type
     */
    @PutMapping
    @Log(title = "房型管理", businessType = "UPDATE", operType = "修改")
    public Result update(@RequestBody DormRoomType roomType) {
        return Result.success(dormRoomTypeService.updateById(roomType));
    }

    /**
     * 删除房间类型
     * DELETE /dorm/room-type/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "房型管理", businessType = "DELETE", operType = "删除")
    public Result delete(@PathVariable Long id) {
        return Result.success(dormRoomTypeService.removeById(id));
    }
}
