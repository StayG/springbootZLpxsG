package com.web.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoginInfoAspect {

    // 定义切点，拦截所有带有 @MyAnnotation 注解的方法
    @Pointcut("@annotation(com.web.aop.LogAnnotation)")
    public void loginMethod() {
    }

    // 切入点：匹配所有登录方法
    @Before("loginMethod()")
    public void logBeforeLogin(JoinPoint joinPoint) {

    }

}
