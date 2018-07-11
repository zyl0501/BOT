package com.ray.router.facade.data;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.launcher.Router;
import com.ray.router.facade.Call;
import com.ray.router.facade.callback.DispatchCallBack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public class Request<T> {

    protected T data;
    protected Uri uri;
    protected String path;
    protected Map<String, Object> querys;

    protected Request() {
    }

    public Request(Request<T> request) {
        this.data = request.data;
        this.uri = request.uri;
        this.path = request.path;
        this.querys = request.querys;
    }

    private Request(Uri uri) {
        this.uri = uri;
        this.path = uri.getPath();
    }

    private Request(String path) {
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public T getData() {
        return data;
    }

    public String getPath() {
        return path;
    }

    public Request<T> uri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public Request<T> uri(String url) {
        Uri uri = Uri.parse(url);
        return uri(uri);
    }

    public Request<T> data(T data) {
        this.data = data;
        return this;
    }

    public String getQuery(String key) {
        if (uri == null) {
            return null;
        }
        return uri.getQueryParameter(key);
    }

    public Request<T> withBoolean(String key, Boolean value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withByte(String key, byte value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withInt(String key, int value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withShort(String key, short value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withLong(String key, long value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withFloat(String key, float value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withDouble(String key, double value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public Request<T> withString(String key, String value) {
        if (querys == null) {
            querys = new HashMap<>();
        }
        querys.put(key, value);
        return this;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value)) {
            return Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

    public byte getByte(String key, byte defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public short getShort(String key, short defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = getQuery(key);
        if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                Router.logger.error(Router.logger.getDefaultTag(), e.getMessage());
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public String getString(String key) {
        return getQuery(key);
    }

    public static <T> Request<T> obtain(Uri uri) {
        return new Request<>(uri);
    }

    public static <T> Request<T> obtain(String path) {
        return new Request<>(Uri.parse(path));
    }

    public <RESP> Call<RESP> dispatch(Context context, Class<RESP> resultType, SerializationService serializationService) {
        attachOtherQuery();
        return Router.I().dispatch(context, this, resultType, serializationService);
    }

    public <RESP> Call<RESP> dispatch(Context context) {
        return dispatch(context, null);
    }

    public <RESP> Call<RESP> dispatch(Context context, Class<RESP> resultType) {
        attachOtherQuery();
        return Router.I().dispatch(context, this, resultType, null);
    }

    private void attachOtherQuery() {
        if (querys != null && querys.size() > 0 && uri != null) {
            StringBuilder uriStr = new StringBuilder(uri.toString());
            if (TextUtils.isEmpty(uri.getQuery())) {
                uriStr.append("?");
            } else {
                uriStr.append("&");
            }
            Set<String> keys = querys.keySet();
            for (String key : keys) {
                if (!TextUtils.isEmpty(uri.getQueryParameter(key))) {
                    continue;
                }
                uriStr.append(key).append("=").append(querys.get(key)).append("&");
            }
            if (!TextUtils.isEmpty(uriStr)) {
                uri = Uri.parse(uriStr.substring(0, uriStr.length() - 1));
            }
        }
    }

    public static void queryToBundle(Request<? super Bundle> request) {
        Map<String, Object> querys = request.querys;
        if (querys == null || querys.isEmpty()) {
            return;
        }
        if (request.data == null) {
            request.data = new Bundle();
        }
        Set<String> keys = querys.keySet();
        for (String key : keys) {
            Object value = querys.get(key);
            BundleUtils.putExtra((Bundle) request.data, key, value);
        }
    }
}
