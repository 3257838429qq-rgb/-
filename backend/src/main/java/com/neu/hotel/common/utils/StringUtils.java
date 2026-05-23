package com.neu.hotel.common.utils;

import cn.hutool.core.util.StrUtil;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    public static String defaultIfBlank(String str, String defaultStr) {
        return StrUtil.isBlank(str) ? defaultStr : str;
    }
}
