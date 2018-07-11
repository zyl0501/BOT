package com.ray.router.test.pic;

import com.ray.router.annotation.Interceptor;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.callback.InterceptorCallback;

/**
 * Created by zyl on 2017/9/25.
 */

@Interceptor
public class RootInterceptor implements IInterceptor {
    @Override
    public void intercept(Chain chain, InterceptorCallback callback) {
        callback.onContinue(chain.request());
    }
}
