package com.ray.router.core;

import com.ray.router.facade.data.Response;
import com.ray.router.facade.Call;
import com.ray.router.facade.callback.IActionCallback;

/**
 * Created by zyl on 2017/7/12.
 */

public class ErrorCall<T> implements Call<T> {

    private Exception exp;

    public ErrorCall(Exception exp) {
        this.exp = exp;
    }

    @Override
    public  Call<T> execute(IActionCallback callback) {
        if(callback != null){
            callback.onException(exp);
        }
        return this;
    }

    @Override
    public  Call<T> enqueue(IActionCallback callback) {
        execute(callback);
        return this;
    }

    @Override
    public void cancel() {

    }
}
