package com.i2i.intern.cellenta.aom.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(public * com.i2i.intern.cellenta.aom..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        String argsAsString = Arrays.toString(joinPoint.getArgs());

        log.info("[START] Method: {} | With args: {}", methodName, argsAsString);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[END] Method: {} | Duration: {} | With result: {}", methodName, duration, result);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[ERROR] Method: {} | Exception: {}", methodName, ex.getMessage(), ex);
            throw ex;
        }
    }
}
