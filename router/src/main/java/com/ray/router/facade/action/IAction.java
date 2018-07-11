package com.ray.router.facade.action;

import android.content.Context;

import com.ray.router.facade.data.Request;
import com.ray.router.facade.callback.IActionResultCallback;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public interface IAction<I, O> {
    void invoke(Context context, Request<I> request, IActionResultCallback<O> callback);

    void cancel();
}
