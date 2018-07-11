package com.ray.router.facade.callback;


import com.ray.router.facade.data.Request;

/**
 * The callback of interceptor.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/4 17:36
 */
public interface InterceptorCallback {

    /**
     * Continue process
     *
     */
    void onContinue(Request request);

    /**
     * Interrupt process, pipeline will be destory when this method called.
     *
     * @param exception Reson of interrupt.
     */
    void onInterrupt(Throwable exception);
}
