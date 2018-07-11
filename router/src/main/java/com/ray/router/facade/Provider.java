package com.ray.router.facade;


import com.ray.router.facade.action.IAction;

import java.util.Collection;
import java.util.Map;

/**
 * Created by zyl on 2017/7/12.
 */

public interface Provider {
    Collection<Class<? extends IInterceptor>> rootInterceptors();

    Map<String, Collection<Class<? extends IInterceptor>>> interceptors();

    Map<String, Collection<Class<? extends IInterceptor>>> matchInterceptors();

    Map<String, Class<? extends IAction>> actions();

    /**
     * Action input arg type
     */
    Map<Class<? extends IAction>, Class> actionInputClzs();
}
