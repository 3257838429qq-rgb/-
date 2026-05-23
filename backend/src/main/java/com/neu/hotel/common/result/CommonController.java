package com.neu.hotel.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.common.utils.PageUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class CommonController<S extends IService<T>, T> {

    @GetMapping("/list")
    public Result<List<T>> list() {
        return Result.success(getBaseService().list());
    }

    @GetMapping("/{id}")
    public Result<T> getById(@PathVariable Long id) {
        return Result.success(getBaseService().getById(id));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody T entity) {
        return Result.success(getBaseService().save(entity));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody T entity) {
        return Result.success(getBaseService().updateById(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(getBaseService().removeById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<T>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<T> page = getBaseService().page(PageUtil.buildPage(current, size));
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/batch")
    public Result<Boolean> batchDelete(@RequestParam List<Long> ids) {
        return Result.success(getBaseService().removeByIds(ids));
    }

    protected S getBaseService() {
        throw new UnsupportedOperationException("Subclass must implement getBaseService()");
    }
}
