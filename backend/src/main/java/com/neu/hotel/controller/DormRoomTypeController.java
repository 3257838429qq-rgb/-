package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.DormRoomType;
import com.neu.hotel.service.DormRoomTypeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dorm/room-type")
public class DormRoomTypeController {

    private final DormRoomTypeService dormRoomTypeService;

    public DormRoomTypeController(DormRoomTypeService dormRoomTypeService) {
        this.dormRoomTypeService = dormRoomTypeService;
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(dormRoomTypeService.selectAll());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(dormRoomTypeService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody DormRoomType roomType) {
        return Result.success(dormRoomTypeService.save(roomType));
    }

    @PutMapping
    public Result update(@RequestBody DormRoomType roomType) {
        return Result.success(dormRoomTypeService.updateById(roomType));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(dormRoomTypeService.removeById(id));
    }
}
