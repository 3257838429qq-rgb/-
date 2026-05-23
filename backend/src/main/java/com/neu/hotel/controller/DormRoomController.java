package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.entity.DormRoomType;
import com.neu.hotel.service.DormRoomService;
import com.neu.hotel.service.DormRoomTypeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dorm/room")
public class DormRoomController {

    private final DormRoomService dormRoomService;
    private final DormRoomTypeService dormRoomTypeService;

    public DormRoomController(DormRoomService dormRoomService, DormRoomTypeService dormRoomTypeService) {
        this.dormRoomService = dormRoomService;
        this.dormRoomTypeService = dormRoomTypeService;
    }

    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          DormRoom room) {
        return Result.success(dormRoomService.selectPage(current, size, room));
    }

    @GetMapping("/available")
    public Result getAvailable(@RequestParam(defaultValue = "1") Long current,
                               @RequestParam(defaultValue = "10") Long size) {
        return Result.success(dormRoomService.selectAvailableRooms(current, size));
    }

    @GetMapping("/types")
    public Result getRoomTypes() {
        return Result.success(dormRoomTypeService.selectAll());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(dormRoomService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody DormRoom room) {
        return Result.success(dormRoomService.save(room));
    }

    @PutMapping
    public Result update(@RequestBody DormRoom room) {
        return Result.success(dormRoomService.updateById(room));
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(dormRoomService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(dormRoomService.removeById(id));
    }
}
