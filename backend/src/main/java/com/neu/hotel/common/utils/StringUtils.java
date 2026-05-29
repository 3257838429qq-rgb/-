package com.neu.hotel.common.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 *
 * <p>对 String 进行空值判断和默认值替换的快捷封装，
 * 简化 Controller 和 Service 层中的字符串判空逻辑。
 *
 * <p>注意：isEmpty 同时判断 null 和空白字符串（trim后为空）；
 * isBlank 额外判断空白字符，由 Hutool 的 StrUtil 提供。
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     *
     * <p>字符串为空的条件：str == null || str.trim().isEmpty()。
     * 即 null、空字符串（""）、仅包含空白字符的字符串均返回 true。
     *
     * @param str 待检查的字符串
     * @return 字符串为空返回 true，否则返回 false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空
     *
     * @param str 待检查的字符串
     * @return 字符串非空返回 true，否则返回 false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 如果字符串为空则返回默认值
     *
     * <p>isEmpty(str) 为 true 时返回 defaultStr，否则返回原字符串。
     *
     * @param str       待检查的字符串
     * @param defaultStr 为空时的默认值
     * @return 原字符串（不为空时）或默认值
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 如果字符串为空白则返回默认值
     *
     * <p>Hutool StrUtil.isBlank 的包装，包含 null、空串、纯空白字符三种情况。
     *
     * @param str        待检查的字符串
     * @param defaultStr 为空白时的默认值
     * @return 原字符串（非空白时）或默认值
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return StrUtil.isBlank(str) ? defaultStr : str;
    }
}
