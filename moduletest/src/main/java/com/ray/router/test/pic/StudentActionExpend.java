package com.ray.router.test.pic;

import com.ray.router.annotation.Action;
import com.ray.router.annotation.Interceptor;

/**
 * Created by zyl on 2017/9/28.
 */

@Action(path = "/student3")
@Interceptor(clz = UserInterceptor.class)
public class StudentActionExpend extends StudentAction {
    @Override
    public String toString() {
        int j;
        return "StudentActionExpend{}";
    }
}
