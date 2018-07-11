package com.ray.router.rx;

import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;

/**
 * Created by zyl on 2017/8/7.
 */
public class RouterError extends Throwable {
    private Response response;
    private Request request;

    public RouterError(Request request,Response response) {
        super();
        this.request = request;
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public Request getRequest() {
        return request;
    }
}
