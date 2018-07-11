package com.ray.router.test.pic;

import android.content.Context;

import com.ray.router.annotation.Action;
import com.ray.router.annotation.Interceptor;
import com.ray.router.facade.action.ModuleAction;
import com.ray.router.facade.callback.IActionResultCallback;
import com.ray.router.facade.data.Request;
import com.ray.router.facade.data.Response;

/**
 * Created by qq448 on 2017/9/21.
 */
@Action(path = "/student", inputClz = Integer.class)
@Interceptor(clz = UserInterceptor.class)
public class StudentAction implements ModuleAction<Integer, StudentModule> {
  @Override
  public void invoke(Context context, Request<Integer> request, IActionResultCallback<StudentModule> callback) {
    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    int id = request.getInt("id", -1);
    if(id == -1){
      callback.onException(new IllegalArgumentException("id is null"));
    }else if(id == 1){
      callback.onResponse(Response.createSuccess(new StudentModule(1, "Student 1", 11)));
    }else if(id == 2){
      callback.onResponse(Response.createSuccess(new StudentModule(2, "Student 2", 22)));
    }else{
      callback.onResponse(Response.<StudentModule>createFailure("id: "+id +" no data"));
    }
  }

  @Override
  public void cancel() {
  }
}
