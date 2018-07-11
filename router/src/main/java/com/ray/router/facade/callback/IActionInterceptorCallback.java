package com.ray.router.facade.callback;

import com.ray.router.facade.data.Request;

/**
 * Created by zyl on 2017/9/19.
 */

public interface IActionInterceptorCallback {
    void onIntercept(Request request);
}
