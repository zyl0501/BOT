package com.ray.router.converter;

import com.ray.router.facade.service.SerializationService;

import java.lang.reflect.Type;

/**
 * Created by zyl on 2017/9/20.
 */

public class SerializationServiceBundle implements SerializationService {
    @Override
    public Object convertReq(Object reqObj, Type actionReqType) {
        return reqObj;
    }

    @Override
    public <T> T convertResp(Object actionRespObj, Type type) {
        return (T)actionRespObj;
    }
}
