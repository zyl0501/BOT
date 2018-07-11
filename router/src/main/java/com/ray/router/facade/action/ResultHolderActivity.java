package com.ray.router.facade.action;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.ray.router.facade.data.Response;
import com.ray.router.facade.callback.IActionResultCallback;

/**
 * Created by zyl on 2017/9/19.
 */

public class ResultHolderActivity extends Activity {
    private static final int RC_HOLDER_START = 100;
    public static final SparseArray<IActionResultCallback<Bundle>> callbacks = new SparseArray<>();

    private int id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Intent originIntent = intent.getParcelableExtra("intent");
        id = intent.getIntExtra("id", -1);
        startActivityForResult(originIntent, RC_HOLDER_START);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IActionResultCallback<Bundle> callback = callbacks.get(id);
        if (callback != null && requestCode == RC_HOLDER_START) {
            if (resultCode == RESULT_OK) {
                callback.onResponse(Response.createSuccess(data == null ? null : data.getExtras()));
            } else if (resultCode == RESULT_CANCELED) {
                callback.onResponse(Response.createCancel(data == null ? null : data.getExtras()));
            } else {
                callback.onResponse(Response.<Bundle>createFailure("on result failure"));
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        callbacks.remove(id);
        super.onDestroy();
    }
}
