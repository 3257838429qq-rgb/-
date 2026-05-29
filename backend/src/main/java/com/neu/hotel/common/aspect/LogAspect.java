package com.neu.hotel.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.entity.SysOperLog;
import com.neu.hotel.mapper.SysOperLogMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 日志切面
 *
 * 【功能说明】
 * - 拦截带有@Log注解的方法
 * - 记录操作日志到数据库
 * - 使用异步线程池避免阻塞主线程
 *
 * 【记录内容】
 * - 操作人、IP、URL、请求方法
 * - 操作类型（增删改查）
 * - 方法名、参数、执行时间
 * - 操作结果和错误信息
 *
 * 【使用方式】
 * 在Controller方法上添加@Log注解：
 * @Log(title = "部门管理", businessType = "INSERT", operType = "新增")
 *
 * 【异步处理】
 * - 使用线程池异步保存日志
 * - 避免日志写入影响接口响应时间
 * - 线程池配置：核心2线程，最大4线程，队列容量100
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    private ExecutorService executor;
    private SysOperLogMapper sysOperLogMapper;

    public LogAspect(SysOperLogMapper sysOperLogMapper) {
        this.sysOperLogMapper = sysOperLogMapper;
    }

    /**
     * 初始化线程池
     */
    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(
                2,  // 核心线程数
                4,  // 最大线程数
                60L, TimeUnit.SECONDS,  // 线程空闲存活时间
                new ArrayBlockingQueue<>(100),  // 队列容量
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * 环绕通知：拦截@Log注解的方法
     * 记录方法执行的开始、结束、耗时和结果
     */
    @Around("@annotation(com.neu.hotel.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        SysOperLog operLog = new SysOperLog();

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            operLog.setStatus(1);  // 成功
            operLog.setResult(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            operLog.setStatus(0);  // 失败
            operLog.setErrorMsg(e.getMessage());
            throw e;  // 继续抛出异常
        } finally {
            // 计算耗时
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            // 异步保存日志
            saveLogAsync(joinPoint, operLog);
        }

        return result;
    }

    /**
     * 异步保存日志
     */
    @Async
    public void saveLogAsync(ProceedingJoinPoint joinPoint, SysOperLog operLog) {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                operLog.setOperIp(request.getRemoteAddr());  // 操作人IP
                operLog.setOperUrl(request.getRequestURI());  // 请求URL
                operLog.setUserId(getUserIdFromRequest(request));  // 操作人ID
            }

            // 获取方法签名和@Log注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation != null) {
                operLog.setBusinessType(logAnnotation.businessType());  // 业务类型
                operLog.setOperType(logAnnotation.operType());  // 操作类型
                operLog.setTitle(logAnnotation.title());  // 操作标题
            }

            // 设置方法信息
            operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
            operLog.setRequestMethod(request != null ? request.getMethod() : "UNKNOWN");
            operLog.setOperTime(LocalDateTime.now());

            // 设置请求参数（限制长度）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = JSONUtil.toJsonStr(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                operLog.setOperParam(params);
            }

            // 保存日志
            if (sysOperLogMapper != null) {
                sysOperLogMapper.insert(operLog);
            }
        } catch (Exception e) {
            log.error("Save operation log failed: {}", e.getMessage());
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
