package com.ray.router.rx;

import com.ray.router.facade.data.Request;

/**
 * Created by zyl on 2017/8/7.
 */
public class CancelError extends Throwable {
    private Request request;

    public CancelError(Request request) {
        super();
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
