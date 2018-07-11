package com.ray.router.facade.data;

import java.io.Closeable;
import java.io.IOException;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public class Response<T> implements Closeable {
    public static final int CODE_SUCCESS = 2;
    public static final int CODE_FAILURE = -1;
    public static final int CODE_USER_CANCEL = -2;

    private int code = CODE_FAILURE;
    private String msg = "";
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public void close() throws IOException {
    }

    public static <T> Response<T> createSuccess(T data){
        return createResp(data, CODE_SUCCESS, "success");
    }

    public static <T> Response<T> createFailure(String msg){
        return createResp(null, CODE_FAILURE, msg);
    }

    public static <T> Response<T> createCancel(T data){
        return createResp(data, CODE_USER_CANCEL, "cancel");
    }

    private static <T> Response<T> createResp(T data, int code, String msg){
        Response<T> response = new Response<>();
        response.setData(data);
        response.code = code;
        response.msg = msg;
        return response;
    }
}
