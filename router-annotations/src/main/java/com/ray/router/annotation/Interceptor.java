package com.ray.router.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;

/**
 * Created by zyl on 2017/7/5.
 */
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {
    String path() default "";
    String pattern() default "";
    int priority() default -1;
    String name() default "Default";

    /**
     * 用于Action的注解
     */
    Class[] clz() default Void.class;
}
