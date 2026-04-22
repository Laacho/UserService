package sit.tuvarna.bg.userservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class LoggingAspect {

    @Before("@annotation(sit.tuvarna.bg.userservice.aop.Loggable)")
    public void logMethodStart(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Starting method: {}.{}", className, methodName);
    }

    @After("@annotation(sit.tuvarna.bg.userservice.aop.Loggable)")
    public void logMethodEnd(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Finished method: {}.{}", className, methodName);
    }
}
