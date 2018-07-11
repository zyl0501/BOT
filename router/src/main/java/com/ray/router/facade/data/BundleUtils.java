package com.ray.router.facade.data;

import android.os.Bundle;

/**
 * Created by Administrator on 2018/3/16 0016.
 */

public class BundleUtils {
    public static void putExtra(Bundle bundle, String key, Object obj) {
        if (bundle != null && obj != null) {
            if (obj instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) obj);
            } else if (obj instanceof Byte) {
                bundle.putByte(key, (Byte) obj);
            } else if (obj instanceof Integer) {
                bundle.putInt(key, (Integer) obj);
            } else if (obj instanceof Long) {
                bundle.putLong(key, (Long) obj);
            } else if (obj instanceof Short) {
                bundle.putShort(key, (Short) obj);
            } else if (obj instanceof Float) {
                bundle.putFloat(key, (Float) obj);
            } else if (obj instanceof Double) {
                bundle.putDouble(key, (Double) obj);
            } else if (obj instanceof String) {
                bundle.putString(key, (String) obj);
            } else if (obj instanceof CharSequence) {
                bundle.putCharSequence(key, (CharSequence) obj);
            }
        }
    }

}
