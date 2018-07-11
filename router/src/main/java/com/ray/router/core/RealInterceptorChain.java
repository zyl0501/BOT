package com.ray.router.core;

import com.ray.router.facade.data.Request;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.callback.InterceptorCallback;
import com.ray.router.launcher.Router;

import java.util.List;

/**
 * Created by zyl on 2017/5/25.
 */

public class RealInterceptorChain implements IInterceptor.Chain {
    private final List<IInterceptor> interceptors;
    private final Request request;
    private int index;

    public RealInterceptorChain(List<IInterceptor> interceptors, int index, Request request) {
        this.interceptors = interceptors;
        this.request = request;
        this.index = index;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public void proceed(final Request request, final InterceptorCallback callback) {
        if (interceptors != null && index < interceptors.size()) {
            final RealInterceptorChain next = new RealInterceptorChain(
                    interceptors, index + 1, request);
            final IInterceptor interceptor = interceptors.get(index);
            interceptor.intercept(next, new InterceptorCallback() {
                @Override
                public void onContinue(Request request) {
                    Router.logger.debug("", interceptor.getClass().getSimpleName() + " continue, request path is " + request.getPath());
                    next.proceed(request, callback);
//                    callback.onContinue(request);
                }

                @Override
                public void onInterrupt(Throwable exception) {
                    Router.logger.debug("", interceptor.getClass().getSimpleName() + " interrupt, request path is " + request.getPath());
                    callback.onInterrupt(exception);
                }
            });
        }else{
            callback.onContinue(request);
        }
    }
}
