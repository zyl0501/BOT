package com.ray.router.facade.service;

import java.lang.reflect.Type;

public interface SerializationService {

    /**
     * 请求参数从调用方转换成应答方的入参
     *
     * @param reqObj 请求方的request
     * @param actionReqType  应答的action泛型的实际类型
     * @return 应答方的request
     */
    Object convertReq(Object reqObj, Type actionReqType);

    /**
     * 应答方返回结果转换为调用方的结果
     * @param actionRespObj 应答方返回的结果
     * @param respClz 请求方结果的class
     * @param <T> 请求方
     * @return 返回给请求方的数据
     */
    <T> T convertResp(Object actionRespObj, Type respClz);

}
