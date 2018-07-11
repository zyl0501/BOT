package com.ray.router.converter;

import com.google.gson.Gson;
import com.ray.router.facade.service.SerializationService;

import java.lang.reflect.Type;

/**
 * Created by zyl on 2017/9/20.
 */

public class SerializationServiceJson implements SerializationService {
    private Gson gson;

    public SerializationServiceJson(){
        gson = new Gson();
    }

    @Override
    public Object convertReq(Object reqObj, Type actionReqType) {
        String json = gson.toJson(reqObj);
        return gson.fromJson(json, actionReqType);
    }

    @Override
    public <T> T convertResp(Object actionRespObj, Type type) {
        String json = gson.toJson(actionRespObj);
       return gson.fromJson(json, type);
    }
}
