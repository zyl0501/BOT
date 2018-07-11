package com.ray.router.rx;

import com.ray.router.facade.Call;
import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * Created by zyl on 2017/8/7.
 */

public class RouterRx {

    public static <REQ, RESP> Observable<RESP> createCallObservable(final Call<RESP> call, final Request<REQ> request) {
        return Observable.create(new ObservableOnSubscribe<RESP>() {
            @Override
            public void subscribe(final ObservableEmitter<RESP> emitter) throws Exception {
                call.execute(new IActionCallback<RESP>() {
                    @Override
                    public void onIntercept(Request request1) {
                        emitter.onError(new InterceptError(request1));
                    }

                    @Override
                    public void onResponse(Response<RESP> response) {
                        int code = response.getCode();
                        if (code == Response.CODE_SUCCESS) {
                            emitter.onNext(response.getData());
                            emitter.onComplete();
                        } else if (code == Response.CODE_USER_CANCEL) {
                            emitter.onError(new CancelError(request));
                        } else {
                            emitter.onError(new RouterError(request, response));
                        }
                    }

                    @Override
                    public void onException(Throwable t) {
                        emitter.onError(t);
                    }

                    @Override
                    public void onCancel() {
                        emitter.onError(new CancelError(request));
                    }
                });
            }
        });
    }
}
