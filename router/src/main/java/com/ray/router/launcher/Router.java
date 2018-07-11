package com.ray.router.launcher;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.ray.router.converter.SerializationServiceBundle;
import com.ray.router.converter.SerializationServiceJson;
import com.ray.router.core.AutowiredServiceImpl;
import com.ray.router.core.ErrorCall;
import com.ray.router.core.RouterCall;
import com.ray.router.exception.DispatchException;
import com.ray.router.facade.Call;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.Provider;
import com.ray.router.facade.action.ActivityAction;
import com.ray.router.facade.action.IAction;
import com.ray.router.facade.action.IFragmentAction;
import com.ray.router.facade.action.ModuleAction;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.utils.Logger;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by zyl on 2017/7/7.
 */
public class Router {
    private static boolean DEBUG = false;
    private static final String TAG = Router.class.getSimpleName();
    public static Logger logger = new Logger();

    private SerializationService jsonService = new SerializationServiceJson();
    private SerializationService bundleService = new SerializationServiceBundle();

    protected Router() {
    }

    private static Router instance;
    private Application context;

    public static Router I() {
        if (instance == null) {
            synchronized (Router.class) {
                if (instance == null) {
                    instance = new Router();
                }
            }
        }
        return instance;
    }

    public static void init(Application application) {
        I().context = application;
        registerProviderPlugin();
    }

    public static void debug(boolean debug) {
        Router.DEBUG = debug;
        logger.showLog(debug);
    }

    private static void registerProviderPlugin() {
    }

    private static void registerProvider(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.getConstructor().newInstance();
            if (obj instanceof Provider) {
                registerProvider((Provider) obj);
            }
        } catch (Exception e) {
            logger.error(TAG, "register class error:" + className);
        }
    }

    public static void registerProvider(Provider provider) {
        if (provider != null) {
            Finder.actions.putAll(provider.actions());
            Finder.rootInterceptors.addAll(provider.rootInterceptors());
            Finder.interceptors.putAll(provider.interceptors());
            Finder.matchInterceptors.putAll(provider.matchInterceptors());
            Finder.actionInputClzs.putAll(provider.actionInputClzs());
        }
    }

    public <T> Call<T> dispatch(Context context, Request<?> request, Class<T> resultType, SerializationService serializationService) {
        String uriStr = request.getPath();
        logger.debug(TAG, "dispatch: " + uriStr);
        Class<? extends IAction> actionClz = Finder.actions.get(uriStr);
        if (actionClz == null) {
            return new ErrorCall<>(new DispatchException("dispatch action lost"));
        }

        IAction action;
        try {
            action = Finder.getAction(uriStr);
        } catch (Exception e) {
            return new ErrorCall<>(e);
        }
        if (serializationService == null) {
            if (action instanceof ModuleAction) {
                serializationService = jsonService;
            } else if (action instanceof ActivityAction) {
                serializationService = bundleService;
                Request.queryToBundle((Request<Bundle>) request);
            } else if (action instanceof IFragmentAction) {
                serializationService = bundleService;
                Request.queryToBundle((Request<Bundle>) request);
            } else {
                serializationService = jsonService;
            }
        }
        Context ctx = context == null ? this.context : context;
        List<IInterceptor> interceptorList = Finder.getInterceptors(uriStr);
        Type inputType = Finder.actionInputClzs.get(action.getClass());
        return new RouterCall<>(ctx, action, request, serializationService, inputType, resultType, interceptorList);
    }

    public <T> Request<T> build(String path) {
        return Request.obtain(path);
    }

    public <T> Request<T> build(Uri uri) {
        return Request.obtain(uri);
    }

    public void inject(Object obj) {
        AutowiredServiceImpl.I().autowire(obj);
    }

    public <T> T service(Class<T> clz) {
        return Finder.service(clz);
    }
}
