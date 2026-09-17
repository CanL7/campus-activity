package org.springdata.campusactivityapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springdata.campusactivityapi.common.CurrentUser;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class OperationLogAspect {
    @Around("@annotation(org.springdata.campusactivityapi.annotation.RequireAdmin)")
    public Object logOperation(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("[操作日志] user={} method={} args={} cost={}ms result=ok",
                    CurrentUser.getUserId(), pjp.getSignature().getName(), pjp.getArgs(), cost);
            return result;
        }catch (Throwable e){
            long cost = System.currentTimeMillis() - start;      // start 在外面，能用
            log.warn("[操作日志] user={} method={} cost={}ms result=FAIL:{}",
                    CurrentUser.getUserId(), pjp.getSignature().getName(),
                    cost, e.getClass().getSimpleName(),e);                 // ← 占位符
            throw e;
        }


    }
}