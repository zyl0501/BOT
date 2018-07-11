package com.ray.router.launcher;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ray.router.exception.ActionException;
import com.ray.router.exception.DispatchException;
import com.ray.router.facade.IInterceptor;
import com.ray.router.facade.action.IAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qq448 on 2018/3/18.
 */

class Finder {
    private static final String SEPARATOR = "$$";
    private static final String SUFFIX_SERVICE = SEPARATOR + "Impl";

    static Collection<Class<? extends IInterceptor>> rootInterceptors = new ArrayList<>();
    static Map<String, Collection<Class<? extends IInterceptor>>> interceptors = new HashMap<>();
    static Map<String, Collection<Class<? extends IInterceptor>>> matchInterceptors = new HashMap<>();
    static Map<String, Class<? extends IAction>> actions = new HashMap<>();
    static Map<Class<? extends IAction>, Class> actionInputClzs = new HashMap<>();

    @NonNull
    static List<IInterceptor> getInterceptors(String uriStr) {
        List<IInterceptor> interceptorList = new ArrayList<>();
        Collection<? extends IInterceptor> list = getInterceptor(rootInterceptors);
        if (list != null && list.size() > 0) {
            interceptorList.addAll(list);
        }

        Set<String> interceptKeys = interceptors.keySet();
        for (String url : interceptKeys) {
            if (TextUtils.equals(url, uriStr)) {
                list = getInterceptor(interceptors.get(url));
                if (list != null && list.size() > 0) {
                    interceptorList.addAll(list);
                }
            }
        }

        interceptKeys = matchInterceptors.keySet();
        for (String pattern : interceptKeys) {
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(uriStr);
            if (m.find()) {
                list = getInterceptor(matchInterceptors.get(pattern));
                if (list != null && list.size() > 0) {
                    interceptorList.addAll(list);
                }
            }
        }
        return interceptorList;
    }

    static IAction getAction(String uriStr) throws Exception {
        Class<? extends IAction> actionClz = Finder.actions.get(uriStr);
        if (actionClz == null) {
            throw new DispatchException("dispatch action lost");
        }
        IAction action = ClassHolder.actions.get(actionClz);
        if (action == null) {
            try {
                action = actionClz.getConstructor().newInstance();
                ClassHolder.actions.put(actionClz, action);
            } catch (Exception e) {
                Router.logger.error("", "Init action failed! " + e.getMessage());
                throw new ActionException("Init action failed!");
            }
        }
        return action;
    }

    static <T> T service(Class<T> clz) {
        try {
            T service = (T) ClassHolder.services.get(clz);
            if (null == service) {  // No cache.
                service = (T) Class.forName(clz.getName() + SUFFIX_SERVICE).getConstructor().newInstance();
                ClassHolder.services.put(clz, service);
            }
            return service;
        } catch (Exception ex) {
            return null;// This instance need not autowired.
        }
    }

    private static Collection<? extends IInterceptor> getInterceptor(Collection<Class<? extends IInterceptor>> clzList) {
        if (clzList != null && clzList.size() > 0) {
            List<IInterceptor> interceptorList = new ArrayList<>(clzList.size());
            for (Class<? extends IInterceptor> clz : clzList) {
                IInterceptor interceptor = getInterceptor(clz);
                if (interceptor != null) {
                    interceptorList.add(interceptor);
                }
            }
            return interceptorList;
        } else {
            return null;
        }
    }

    private static IInterceptor getInterceptor(Class<? extends IInterceptor> interceptorClz) {
        IInterceptor interceptor = ClassHolder.interceptors.get(interceptorClz);
        if (interceptor == null) {
            try {
                interceptor = interceptorClz.getConstructor().newInstance();
                ClassHolder.interceptors.put(interceptorClz, interceptor);
            } catch (Exception e) {
                Router.logger.error("", "Init action failed! " + e.getMessage());
                return null;
            }
        }
        return interceptor;
    }
}
