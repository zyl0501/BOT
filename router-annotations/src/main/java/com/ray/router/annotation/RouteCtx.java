package com.ray.router.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * @author zyl
 * @date Created on 2018/2/26
 */
@Documented
@Target(PARAMETER)
@Retention(CLASS)
public @interface RouteCtx {
}
