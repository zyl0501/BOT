package com.ray.router.facade.callback;

import com.ray.router.facade.data.Request;

/**
 * Created by zyl on 2017/7/12.
 */

public interface DispatchCallBack {
    /**
     * IActionCallback when find the destination.
     *
     * @param request meta
     */
    void onFound(Request request);

    /**
     * IActionCallback after lose your way.
     *
     * @param request meta
     */
    void onLost(Request request);

}
