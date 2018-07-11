package com.ray.router.facade.callback;

import com.ray.router.facade.data.Response;

/**
 * Created by zyl on 2017/9/19.
 */

public interface IActionResultCallback<T> {
    void onResponse(Response<T> response);

    void onException(Throwable t);

    void onCancel();
}
