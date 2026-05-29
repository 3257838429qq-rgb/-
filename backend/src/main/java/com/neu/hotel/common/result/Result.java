package com.neu.hotel.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装类
 *
 * <p>所有 Controller 接口的返回值统一通过此类进行包装，
 * 包含状态码、消息和业务数据三部分。
 *
 * <p>状态码约定：
 * <ul>
 *   <li>200 - 成功</li>
 *   <li>401 - 未认证（未登录或 token 失效）</li>
 *   <li>403 - 无权限（已登录但无权限访问）</li>
 *   <li>500 - 服务器内部错误</li>
 * </ul>
 *
 * @param <T> 泛型，指定 data 字段的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应状态码，200 表示成功，其他值表示异常
     */
    private Integer code;

    /**
     * 操作结果的中文描述信息
     */
    private String message;

    /**
     * 业务数据，可为 null
     */
    private T data;

    /**
     * 返回成功结果（无数据）
     *
     * @param <T> 泛型
     * @return code=200, message="操作成功", data=null 的 Result
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 返回成功结果（带数据）
     *
     * @param data 业务数据
     * @param <T>  泛型
     * @return code=200, message="操作成功", data=传入数据的 Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 返回成功结果（自定义消息和数据）
     *
     * @param message 自定义消息
     * @param data    业务数据
     * @param <T>     泛型
     * @return code=200, 自定义 message, data=传入数据的 Result
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 返回失败结果（默认消息）
     *
     * @param <T> 泛型
     * @return code=500, message="操作失败", data=null 的 Result
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }

    /**
     * 返回失败结果（自定义消息）
     *
     * @param message 错误描述
     * @param <T>     泛型
     * @return code=500, 自定义 message, data=null 的 Result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 返回失败结果（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误描述
     * @param <T>     泛型
     * @return 自定义 code, 自定义 message, data=null 的 Result
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 返回未认证结果（401）
     *
     * @param message 认证失败原因
     * @param <T>     泛型
     * @return code=401, 自定义 message, data=null 的 Result
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    /**
     * 返回无权限结果（403）
     *
     * @param message 权限不足原因
     * @param <T>     泛型
     * @return code=403, 自定义 message, data=null 的 Result
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }
}
