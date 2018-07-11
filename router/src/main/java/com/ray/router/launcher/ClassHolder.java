package com.ray.router.launcher;

import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.action.IAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zyl on 2017/9/25.
 */

class ClassHolder {

    static Map<Class, IInterceptor> interceptors = new HashMap<>();
    static Map<Class, IAction> actions = new HashMap<>();
    static Map<Class, Object> services = new HashMap<>();

    static void clear() {
        interceptors.clear();
        actions.clear();
    }
}
