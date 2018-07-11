package com.ray.router.test.pic;

import android.util.Log;

import com.ray.router.annotation.Interceptor;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.callback.InterceptorCallback;

/**
 * Created by zyl on 2017/9/22.
 */
@Interceptor(path = "/student2")
public class UserInterceptor3 implements IInterceptor {
    @Override
    public void intercept(Chain chain, InterceptorCallback callback) {
        Log.d("raytest", "UserInterceptor");
        callback.onContinue(chain.request());
    }
}
