package com.atguigu.gmall.index.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/14
 * \* Time: 11:18
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAnnotation {

    String prefix() default "";

    int timeout() default 5;

    int random() default 5;

}
