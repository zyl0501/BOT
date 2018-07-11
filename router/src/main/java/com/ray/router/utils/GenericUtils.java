package com.ray.router.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zyl on 2017/4/19.
 */
public class GenericUtils {

    /**
     * Returns the generic supertype for {@code supertype}. For example, given a class {@code
     * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
     * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
     */
    public static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) return context;
        // We skip searching through interfaces if unknown is an interface.
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }
        // Check our supertypes.
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }
        // We can't resolve this further.
        return toResolve;
    }

    public static <T> Type getOneGenericType(T object, Class<T> targetClz) {
        if (object == null) {
            return Void.TYPE;
        }
        Class objectClz = object.getClass();
        Map<Class, Type> classTypeMap = getAllInterfaces(objectClz, null);
        if (classTypeMap != null) {
            Type type = classTypeMap.get(targetClz);
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException();
    }

    private static Map<Class, Type> getAllInterfaces(Class clz, Map<Class, Type> map) {
        if (clz == null) {
            return null;
        }
        if (map == null) {
            map = new HashMap<>();
        }
        while (!Object.class.equals(clz) && clz != null) {
            Class<?>[] interClzArray = clz.getInterfaces();
            Type[] genericInterClzArray = clz.getGenericInterfaces();
            if (interClzArray != null && interClzArray.length > 0) {
                for (int i = 0, length = interClzArray.length; i < length; i++) {
                    map.put(interClzArray[i], genericInterClzArray[i]);
                    getAllInterfaces(interClzArray[i], map);
                }
            }
            clz = clz.getSuperclass();
        }
        return map;
    }
}
