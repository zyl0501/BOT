package com.ray.router;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ray.router.facade.Call;
import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;
import com.ray.router.launcher.Router;
import com.ray.router.route.RouterTestModuleProvider;
import com.ray.router.rx.RouterRx;
import com.ray.router.utils.Handlers;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "raytest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Router.debug(BuildConfig.DEBUG);
        Router.init(getApplication());
//        Router.I().registerProvider(new RouterTestModuleProvider());
    }

    public void routeActivity(View view) {
        Router.I().service(SchoolService.class)
                .test3(this, true, 115)
                .subscribe(new Observer<Bundle>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bundle bundle) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        Router.I().service(SchoolService.class)
//                .testActivity(this, this, true, 111)
////        Router.I().bundle("/school")
////                .withBoolean("b", true)
////                .withInt("school_id", 111)
////                .dispatch(MainActivity.this, Bundle.class, new DispatchCallBack() {
////                    @Override
////                    public void onFound(Request request) {
////
////                    }
////
////                    @Override
////                    public void onLost(Request request) {
////
////                    }
////                })
//                .execute(new IActionCallback<Bundle>() {
//                    @Override
//                    public void onIntercept(Request request) {
//                        Log.d(TAG, request.getPath() + " is intercept");
//                    }
//
//                    @Override
//                    public void onResponse(Response<Bundle> response) {
//                        if (response.getCode() == Response.CODE_SUCCESS) {
//                            Bundle data = response.getData();
//                            Log.d(TAG, "take " + data.getString("student"));
//                        } else {
//                            Log.d(TAG, response.getMsg());
//                        }
//                    }
//
//                    @Override
//                    public void onException(Throwable t) {
//                        Log.d(TAG, t.getMessage());
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        Log.d(TAG, "onCancel");
//                    }
//                });
    }

    public void routeFragment(View view) {
        Bundle bundle = new Bundle();
        Request.<Bundle>obtain("/student_fragment?id=4")
                .data(bundle)
                .dispatch(MainActivity.this, Fragment.class)
                .execute(new IActionCallback<Fragment>() {
                    @Override
                    public void onIntercept(Request request) {
                        Log.d(TAG, request.getPath() + " is intercept");
                    }

                    @Override
                    public void onResponse(Response<Fragment> response) {
                        if (response.getCode() == Response.CODE_SUCCESS) {
                            Fragment fragment = response.getData();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container_layout, fragment)
                                    .commit();
                        } else {
                            Log.d(TAG, response.getMsg());
                        }
                    }

                    @Override
                    public void onException(Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel");
                    }
                });
    }

    public void routeAction(View view) {
        final Call<Student> call = Request.obtain("/student?id=2")
                .<Student>dispatch(MainActivity.this, Student.class)
                .enqueue(new IActionCallback<Student>() {
                    @Override
                    public void onIntercept(Request request) {
                        Log.d(TAG, request.getPath() + " is intercept");
                    }

                    @Override
                    public void onResponse(Response<Student> response) {
                        if (response.getCode() == Response.CODE_SUCCESS) {
                            Log.d(TAG, response.getData().toString());
                        } else {
                            Log.d(TAG, response.getMsg());
                        }
                    }

                    @Override
                    public void onException(Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel");
                    }
                });
        Handlers.postDelayed(new Runnable() {
            @Override
            public void run() {
                call.cancel();
            }
        }, 2000);
    }

    public void notFound(View view) {
        Request.obtain("/xxxx?id=2")
                .dispatch(MainActivity.this, Void.class)
                .execute(new IActionCallback<Void>() {
                    @Override
                    public void onIntercept(Request request) {
                        Log.d(TAG, request.getPath() + " is intercept");
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.getCode() == Response.CODE_SUCCESS) {
                            Log.d(TAG, response.getData().toString());
                        } else {
                            Log.d(TAG, response.getMsg());
                        }
                    }

                    @Override
                    public void onException(Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel");
                    }
                });

    }
}
