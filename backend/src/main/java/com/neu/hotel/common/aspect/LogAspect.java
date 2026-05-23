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

@Aspect
@Component
@Slf4j
public class LogAspect {

    private ExecutorService executor;
    private SysOperLogMapper sysOperLogMapper;

    public LogAspect(SysOperLogMapper sysOperLogMapper) {
        this.sysOperLogMapper = sysOperLogMapper;
    }

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Around("@annotation(com.neu.hotel.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        SysOperLog operLog = new SysOperLog();

        try {
            result = joinPoint.proceed();
            operLog.setStatus(1);
            operLog.setResult(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            operLog.setStatus(0);
            operLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            saveLogAsync(joinPoint, operLog);
        }

        return result;
    }

    @Async
    public void saveLogAsync(ProceedingJoinPoint joinPoint, SysOperLog operLog) {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                operLog.setOperIp(request.getRemoteAddr());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setUserId(getUserIdFromRequest(request));
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation != null) {
                operLog.setBusinessType(logAnnotation.businessType());
                operLog.setOperType(logAnnotation.operType());
                operLog.setTitle(logAnnotation.title());
            }

            operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
            operLog.setRequestMethod(request != null ? request.getMethod() : "UNKNOWN");
            operLog.setOperTime(LocalDateTime.now());

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = JSONUtil.toJsonStr(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                operLog.setOperParam(params);
            }

            if (sysOperLogMapper != null) {
                sysOperLogMapper.insert(operLog);
            }
        } catch (Exception e) {
            log.error("Save operation log failed: {}", e.getMessage());
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
