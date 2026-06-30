package com.web.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  // 这个注解用于方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAnnotation {
}
