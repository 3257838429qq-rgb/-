package com.neu.hotel.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 *
 * <p>用于统一返回分页查询的结果，包含总数、当前页数据列表、分页参数等信息。
 *
 * <p>与前端 Element Plus 等 UI 框架的分页组件无缝对接。
 *
 * @param <T> 泛型，指定 records 列表中元素的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 查询条件匹配的总记录数
     */
    private Long total;

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    /**
     * 当前页码（从 1 开始）
     */
    private Long current;

    /**
     * 每页显示的记录数
     */
    private Long size;

    /**
     * 构造器：仅传入总数和数据列表（适用于不分页的场景）
     *
     * @param total    总记录数
     * @param records  当前页数据
     */
    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
