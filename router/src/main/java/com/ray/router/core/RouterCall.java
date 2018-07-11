package com.ray.router.core;

import android.content.Context;

import com.ray.router.facade.action.IAction;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.Call;
import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.callback.InterceptorCallback;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.launcher.Router;
import com.ray.router.thread.DefaultPoolExecutor;
import com.ray.router.utils.GenericUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public class RouterCall<T> implements Call<T> {

    private Context context;
    private IAction action;
    private Request request;
    private Type returnClz;
    private Type actionInputClz;
    private List<IInterceptor> interceptors;
    private ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    private SerializationService serializationService;
    private FutureTask<T> task;
    private IActionCallback actionCallback;

    public RouterCall(Context context,
                      IAction action,
                      Request request,
                      SerializationService serializationService,
                      Type actionInputClz,
                      Type returnClz,
                      List<IInterceptor> interceptors) {
        this.action = action;
        this.context = context;
        this.request = request;
        this.serializationService = serializationService;
        this.returnClz = returnClz;
        this.actionInputClz = actionInputClz;
        this.interceptors = interceptors;
    }

    @Override
    public Call<T> execute(final IActionCallback<T> callback) {
        if (returnClz == null) {
            returnClz = GenericUtils.getOneGenericType(callback, IActionCallback.class);
        }
        this.actionCallback = callback;
        RealInterceptorChain chain = new RealInterceptorChain(interceptors, 0, request);
        chain.proceed(request, new InterceptorCallback() {
            @Override
            public void onContinue(Request request) {
                Router.logger.debug("RouterCall", "Interceptor onContinue");
                ActionInvoke actionInvoke = new ActionInvoke(context, action, serializationService, returnClz, actionInputClz, callback);
                actionInvoke.invoke(request);
            }

            @Override
            public void onInterrupt(Throwable exception) {
                if (callback != null) {
                    callback.onIntercept(request);
                }
            }
        });
        return this;
    }

    @Override
    public Call<T> enqueue(final IActionCallback<T> callback) {
        task = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                execute(callback);
            }
        }, null);
        executor.execute(task);
        return this;
    }

    @Override
    public void cancel() {
        if (action != null) {
            action.cancel();
            if (actionCallback != null) {
                actionCallback.onCancel();
            }
        }
        if (task != null) {
            task.cancel(true);
        }
    }

}
