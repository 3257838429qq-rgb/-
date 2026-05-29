package com.neu.hotel.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.common.result.PageResult;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * <p>封装 MyBatis-Plus 的分页查询与 PageResult 之间的转换逻辑，
 * 简化 Controller 和 Service 层的分页代码。
 *
 * <p>提供两种转换方式：
 * <ul>
 *   <li>基于目标类的属性拷贝（BeanUtil.copyProperties）</li>
 *   <li>基于自定义转换函数（Function）</li>
 * </ul>
 */
public class PageUtil {

    /**
     * 将 IPage 转换为 PageResult（基于目标类进行属性拷贝）
     *
     * <p>适用于 DO 转 DTO/VO 场景，自动将 page 中的每条记录
     * 通过 BeanUtil.copyProperties 拷贝到 targetClass 类型的新对象中。
     *
     * @param page        MyBatis-Plus 分页结果
     * @param targetClass 目标类型（DTO/VO 的 Class）
     * @param <T>         原始数据类型（DO）
     * @param <R>         目标数据类型（DTO/VO）
     * @return 包含分页元数据和转换后记录的 PageResult
     */
    public static <T, R> PageResult<R> convertPage(IPage<T> page, Class<R> targetClass) {
        List<R> records = page.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, targetClass))
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, page.getCurrent(), page.getSize());
    }

    /**
     * 将 IPage 转换为 PageResult（基于自定义函数进行转换）
     *
     * <p>适用于需要自定义映射逻辑的场景，
     * 例如字段重组、枚举转换、多对象聚合等。
     *
     * @param page      MyBatis-Plus 分页结果
     * @param converter 自定义转换函数 T -> R
     * @param <T>       原始数据类型
     * @param <R>       目标数据类型
     * @return 包含分页元数据和转换后记录的 PageResult
     */
    public static <T, R> PageResult<R> convertPage(IPage<T> page, Function<T, R> converter) {
        List<R> records = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, page.getCurrent(), page.getSize());
    }

    /**
     * 构建 MyBatis-Plus 分页对象
     *
     * <p>对入参进行防御性处理：
     * <ul>
     *   <li>current 为 null 或小于 1 时，默认设为 1</li>
     *   <li>size 为 null 或小于 1 时，默认设为 10</li>
     * </ul>
     *
     * @param current 当前页码（从 1 开始）
     * @param size    每页记录数
     * @return 配置好的 Page 对象
     */
    public static <T> Page<T> buildPage(Long current, Long size) {
        current = current == null || current < 1 ? 1 : current;
        size = size == null || size < 1 ? 10 : size;
        return new Page<>(current, size);
    }
}
