package com.ray.router.core;

import android.content.Context;

import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.action.IAction;
import com.ray.router.facade.callback.IActionResultCallback;
import com.ray.router.facade.callback.InterceptorCallback;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.utils.Handlers;

import java.lang.reflect.Type;

/**
 * Created by zyl on 2017/10/11.
 */

public class ActionInvoke {
    private Context context;
    private IAction action;
    private Type returnClz;
    private Type actionInputClz;
    private IActionResultCallback callback;

    private SerializationHelper serializationHelper;

    ActionInvoke(Context context,
                 IAction action,
                 SerializationService serializationService,
                 Type returnClz,
                 Type actionInputClz,
                 IActionResultCallback callback) {
        this.context = context;
        this.action = action;
        this.returnClz = returnClz;
        this.actionInputClz = actionInputClz;
        this.callback = callback;
        serializationHelper = new SerializationHelper(serializationService);
    }

    /*package*/void invoke(Request request) {
        try {
            Request remoteReq = serializationHelper.parseRequest(request, action, actionInputClz);
            action.invoke(context, remoteReq, new IActionResultCallback() {
                @Override
                public void onResponse(Response rawResp) {
                    if (callback != null) {
                        Response result = rawResp;
                        final Response response = serializationHelper.parseResult(result, returnClz);
                        Handlers.postMain(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(response);
                            }
                        });
                    }
                }

                @Override
                public void onException(final Throwable t) {
                    if (callback != null) {
                        Handlers.postMain(new Runnable() {
                            @Override
                            public void run() {
                                callback.onException(t);
                            }
                        });
                    }
                }

                @Override
                public void onCancel() {
                    if (callback != null) {
                        Handlers.postMain(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCancel();
                            }
                        });
                    }
                }
            });
        } catch (final Exception e) {
            if (callback != null) {
                Handlers.postMain(new Runnable() {
                    @Override
                    public void run() {
                        callback.onException(e);
                    }
                });
            }
        }
    }
}
