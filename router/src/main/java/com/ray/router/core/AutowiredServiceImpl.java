package com.ray.router.core;

import android.util.LruCache;

import com.ray.router.facade.ISyringe;
import com.ray.router.facade.service.AutowiredService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyl on 2017/9/21.
 */

public class AutowiredServiceImpl implements AutowiredService {
    public static final String SDK_NAME = "Router";
    public static final String SEPARATOR = "$$";
    public static final String SUFFIX_AUTOWIRED = SEPARATOR + SDK_NAME + SEPARATOR + "Autowired";

    private LruCache<String, ISyringe> classCache;
    private List<String> blackList;

    private static AutowiredServiceImpl instance;

    public static AutowiredServiceImpl I() {
        if (instance == null) {
            synchronized (AutowiredServiceImpl.class) {
                if (instance == null) {
                    instance = new AutowiredServiceImpl();
                }
            }
        }
        return instance;
    }

    private AutowiredServiceImpl() {
        classCache = new LruCache<>(66);
        blackList = new ArrayList<>();
    }

    @Override
    public void autowire(Object instance) {
        String className = instance.getClass().getName();
        try {
            if (!blackList.contains(className)) {
                ISyringe autowiredHelper = classCache.get(className);
                if (null == autowiredHelper) {  // No cache.
                    autowiredHelper = (ISyringe) Class.forName(instance.getClass().getName() + SUFFIX_AUTOWIRED).getConstructor().newInstance();
                }
                autowiredHelper.inject(instance);
                classCache.put(className, autowiredHelper);
            }
        } catch (Exception ex) {
            blackList.add(className);    // This instance need not autowired.
        }
    }
}
