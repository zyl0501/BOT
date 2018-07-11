package com.ray.router;

import android.content.Context;
import android.os.Bundle;

import com.ray.router.facade.Call;
import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;
import com.ray.router.launcher.Router;
import com.ray.router.rx.RouterRx;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author zyl
 * @date Created on 2018/2/26
 */
public class SchoolServiceImpl implements SchoolService {
    @Override
    public Call<Bundle> testActivity(Context context1, Context context, boolean b, int id) {
        Request<Bundle> request = Router.I().build("/school");
        request.withBoolean("b", b);
        request.withInt("school_id", id);
        Call<Bundle> call = request.dispatch(context, Bundle.class);
        return call;
    }

    @Override
    public Call<String> test(Context context, boolean b, int id) {
        return null;
    }

    @Override
    public Call<String> test2(Context context, boolean b, int id) {
        return null;
    }

    @Override
    public Observable<Bundle> test3(final Context context, final boolean b, final int id) {
        final Request<Bundle> request = Router.I().build("/school");
        request.withBoolean("b", b);
        request.withInt("school_id", id);
        final Call<Bundle> call = request.dispatch(context, Bundle.class);
        Observable<Bundle> observable = RouterRx.createCallObservable(call, request);
        return observable;
    }

}
