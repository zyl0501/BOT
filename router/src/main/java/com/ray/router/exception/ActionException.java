package com.ray.router.exception;

/**
 * Created by zyl on 2017/9/21.
 */

public class ActionException extends RuntimeException {
    public ActionException(String detailMessage) {
        super(detailMessage);
    }
}
