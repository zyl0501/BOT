package com.ray.router.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zyl on 2017/7/5.
 */
@Retention(RetentionPolicy.CLASS)
public @interface Action {
    String path();
    String name() default "Default";

    /**
     * 是否有返回结果。
     * 用于 Activity 注解时，是否需要 OnActivityResult
     */
    boolean hasResult() default false;

    /**
     * 入参类型，如果指定了，则不会使用反射去获取
     */
    Class inputClz() default Void.class;
}