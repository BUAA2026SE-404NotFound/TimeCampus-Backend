package com.notfound.timecampusserver.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class TimeFillAspect {

    @Before("execution(* com.notfound.timecampusserver.mapper.*.insert*(..))")
    public void fillCreateAndUpdateTime(JoinPoint joinPoint) {
        LocalDateTime now = LocalDateTime.now();
        for (Object arg : joinPoint.getArgs()) {
            fill(arg, "setCreateTime", now, false);
            fill(arg, "setUpdateTime", now, true);
        }
    }

    @Before("execution(* com.notfound.timecampusserver.mapper.*.update*(..))")
    public void fillUpdateTime(JoinPoint joinPoint) {
        LocalDateTime now = LocalDateTime.now();
        for (Object arg : joinPoint.getArgs()) {
            fill(arg, "setUpdateTime", now, true);
        }
    }

    private void fill(Object target, String methodName, LocalDateTime value, boolean overwrite) {
        if (target == null) {
            return;
        }
        if (target instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                fill(item, methodName, value, overwrite);
            }
            return;
        }
        try {
            Method getter = target.getClass().getMethod(methodName.replace("set", "get"));
            if (!overwrite && getter.invoke(target) != null) {
                return;
            }
            Method setter = target.getClass().getMethod(methodName, LocalDateTime.class);
            setter.invoke(target, value);
        } catch (ReflectiveOperationException ignored) {
            // Entities without timestamp setters are intentionally ignored.
        }
    }
}
