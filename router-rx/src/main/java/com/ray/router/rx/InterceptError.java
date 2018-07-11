package com.ray.router.rx;

import com.ray.router.facade.data.Request;

/**
 * Created by zyl on 2017/8/7.
 */
public class InterceptError extends Throwable {
    private Request request;

    public InterceptError(Request request) {
        super();
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
