package com.ray.router.facade.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ray.router.facade.data.Request;
import com.ray.router.facade.callback.IActionResultCallback;
import com.ray.router.utils.ActivityUtils;

/**
 * 创建时间：2017/3/13
 *
 * @author zyl
 */
public abstract class ActivityAction implements IAction<Bundle, Bundle> {
    private static int id = 0;
    //是否需要返回结果
    protected boolean needResult = false;

    @Override
    public final void invoke(Context context, Request<Bundle> request, IActionResultCallback<Bundle> callback) {
        try {
            if (id == Integer.MAX_VALUE) id = 0;
            Intent originIntent = createIntent(context, request);
            if (needResult) {
                Intent intent = new Intent(context, ResultHolderActivity.class);
                intent.putExtra("intent", originIntent);
                intent.putExtra("id", ++id);
                ResultHolderActivity.callbacks.put(id, callback);
                ActivityUtils.startActivitySafe(context, intent);
            } else {
                ActivityUtils.startActivitySafe(context, originIntent);
            }
        } catch (Exception e) {
            callback.onException(e);
        }
    }

    protected abstract Intent createIntent(Context context, Request<Bundle> request) throws Exception;

    public void cancel(){}
}
