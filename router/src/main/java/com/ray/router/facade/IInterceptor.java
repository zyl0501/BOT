package com.ray.router.facade;

import com.ray.router.facade.data.Request;
import com.ray.router.facade.callback.InterceptorCallback;

public interface IInterceptor {
    void intercept(Chain chain, InterceptorCallback callback);

    interface Chain {
        Request request();

        void proceed(Request request, InterceptorCallback callback);
    }
}
