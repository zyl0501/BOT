package com.ray.router.core;

import com.ray.router.facade.action.IAction;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;
import com.ray.router.facade.service.SerializationService;
import com.ray.router.utils.GenericUtils;

import java.lang.reflect.Type;

/**
 * Created by zyl on 2017/9/20.
 */

class SerializationHelper {

    private SerializationService service;

    SerializationHelper(SerializationService service) {
        this.service = service;
    }

    /**
     * 请求参数从调用方转换成应答方的入参，生成一个新的request
     *
     * @param callerRequest 请求方的request
     * @param targetAction  应答的action，用来获取泛型的实际类型
     * @return 应答方的request
     */
    Request parseRequest(Request callerRequest, IAction targetAction, Type adviseInputType) {
        Request<Object> remoteReq = new Request<Object>(callerRequest);
        Object reqData = callerRequest.getData();
        if (reqData != null) {
            if (adviseInputType == null)
                adviseInputType = GenericUtils.getOneGenericType(targetAction, IAction.class);
            Object remoteReqObj = service.convertReq(reqData, adviseInputType);
            remoteReq.data(remoteReqObj);
        }
        remoteReq.uri(callerRequest.getUri());
        return remoteReq;
    }

    Response parseResult(Response remoteResult, Type respClz) {
        Response response = new Response();
        if (remoteResult != null) {
            response.setCode(remoteResult.getCode());
            response.setMsg(remoteResult.getMsg());
            if (remoteResult.getData() != null) {
                response.setData(service.convertResp(remoteResult.getData(), respClz));
            }
        }
        return response;
    }

}
