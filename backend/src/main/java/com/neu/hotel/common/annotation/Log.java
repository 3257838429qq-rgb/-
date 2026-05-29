package com.neu.hotel.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * <p>标记在 Controller 方法上，配合 {@code LogAspect} 切面实现自动化的操作日志记录。
 * 切面拦截所有带此注解的方法，提取注解属性和请求信息后异步写入数据库。
 *
 * <p><b>使用示例：</b>
 * <pre>
 * &#64;Log(title = "用户管理", businessType = "INSERT", operType = "新增用户")
 * &#64;PostMapping
 * public Result&lt;Boolean&gt; add(&#64;RequestBody User user) {
 *     return Result.success(userService.save(user));
 * }
 * </pre>
 *
 * <p><b>属性说明：</b>
 * <ul>
 *   <li>title        - 操作模块标题，如"用户管理"、"部门管理"</li>
 *   <li>businessType - 业务操作类型，对应枚举如 INSERT/UPDATE/DELETE/SELECT 等</li>
 *   <li>operType     - 操作类型描述，如"新增用户"、"删除角色"</li>
 *   <li>operatorType - 操作者类型，如"后台用户"、"前台访客"</li>
 * </ul>
 *
 * @see com.neu.hotel.common.aspect.LogAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块的标题
     * <p>用于标识哪个业务模块，如"用户管理"、"房间管理"
     */
    String title() default "";

    /**
     * 业务操作类型
     * <p>建议取值：INSERT（新增）、UPDATE（修改）、DELETE（删除）、SELECT（查询）、OTHER（其他）
     */
    String businessType() default "OTHER";

    /**
     * 操作类型的中文描述
     * <p>如"新增用户"、"修改密码"、"删除房间"
     */
    String operType() default "操作";

    /**
     * 操作者类型
     * <p>标识操作人的身份，如"后台用户"、"前台访客"
     */
    String operatorType() default "后台用户";
}
