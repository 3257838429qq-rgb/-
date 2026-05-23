package com.neu.hotel.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String title() default "";

    String businessType() default "OTHER";

    String operType() default "操作";

    String operatorType() default "后台用户";
}
