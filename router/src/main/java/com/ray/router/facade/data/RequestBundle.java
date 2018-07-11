package com.ray.router.facade.data;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.ray.router.facade.callback.DispatchCallBack;
import com.ray.router.facade.callback.IActionCallback;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.launcher.Router;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public class RequestBundle extends Request<Bundle> {

    protected RequestBundle(){
        super();
    }

    public RequestBundle(Request<Bundle> request) {
        this.data = request.data;
        this.uri = request.uri;
        this.path = request.path;
        this.querys = request.querys;
    }

    private RequestBundle(Uri uri) {
        this.uri = uri;
        this.path = uri.getPath();
    }

    private RequestBundle(String path) {
        this.path = path;
    }

    public RequestBundle uri(String url) {
        return (RequestBundle)super.uri(url);
    }

    public RequestBundle data(Bundle data) {
        return (RequestBundle)super.data(data);
    }

    public RequestBundle withBoolean(String key, Boolean value){
       super.withBoolean(key, value);
       if(data == null) data = new Bundle();
       data.putBoolean(key, value);
       return this;
    }

    public RequestBundle withByte(String key, byte value){
        super.withByte(key, value);
        if(data == null) data = new Bundle();
        data.putByte(key, value);
        return this;
    }

    public RequestBundle withInt(String key, int value){
        super.withInt(key, value);
        if(data == null) data = new Bundle();
        data.putInt(key, value);
        return this;
    }

    public RequestBundle withShort(String key, short value){
        super.withShort(key, value);
        if(data == null) data = new Bundle();
        data.putShort(key, value);
        return this;
    }

    public RequestBundle withLong(String key, long value){
        super.withLong(key, value);
        if(data == null) data = new Bundle();
        data.putLong(key, value);
        return this;
    }

    public RequestBundle withFloat(String key, float value){
        super.withFloat(key, value);
        if(data == null) data = new Bundle();
        data.putFloat(key, value);
        return this;
    }

    public RequestBundle withDouble(String key, double value){
        super.withDouble(key, value);
        if(data == null) data = new Bundle();
        data.putDouble(key, value);
        return this;
    }

    public RequestBundle withString(String key, String value){
        super.withString(key, value);
        if(data == null) data = new Bundle();
        data.putString(key, value);
        return this;
    }

    public static RequestBundle obtain(Uri uri) {
        return new RequestBundle(uri);
    }

    public static RequestBundle obtain(String path) {
        return new RequestBundle(Uri.parse(path));
    }

}
