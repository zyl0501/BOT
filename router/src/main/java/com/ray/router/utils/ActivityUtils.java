package com.ray.router.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zyl on 2017/9/19.
 */

public class ActivityUtils {
    private static Logger logger = new Logger();

    public static void startActivitySafe(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            logger.debug("activity", e.getMessage());
        }
    }

    public static boolean startActivityForResultSafe(Context context, Intent intent, int requestCode) {
        if (context == null || intent == null) return false;
        if (!(context instanceof Activity)) {
            return false;
        }
        try {
            ((Activity) context).startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException e) {
            logger.debug("activity", e.getMessage());
            return false;
        }
    }
}
