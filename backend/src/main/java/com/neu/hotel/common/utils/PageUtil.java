package com.neu.hotel.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.common.result.PageResult;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageUtil {

    public static <T, R> PageResult<R> convertPage(IPage<T> page, Class<R> targetClass) {
        List<R> records = page.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, targetClass))
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, page.getCurrent(), page.getSize());
    }

    public static <T, R> PageResult<R> convertPage(IPage<T> page, Function<T, R> converter) {
        List<R> records = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, page.getCurrent(), page.getSize());
    }

    public static <T> Page<T> buildPage(Long current, Long size) {
        current = current == null || current < 1 ? 1 : current;
        size = size == null || size < 1 ? 10 : size;
        return new Page<>(current, size);
    }
}
