package com.ray.router.utils;

import android.os.Handler;
import android.os.Looper;

public final class Handlers {
    public final static Handler MAIN = new Handler(Looper.getMainLooper());

    public static void postMain(Runnable runnable) {
        MAIN.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayMillis) {
        MAIN.postDelayed(runnable, delayMillis);
    }

    public static void removeCallbacks(Runnable runnable) {
        MAIN.removeCallbacks(runnable);
    }
}
