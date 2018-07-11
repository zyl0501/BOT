package com.ray.router.facade;

import com.ray.router.facade.callback.IActionCallback;

/**
 * 创建时间：2017/3/2
 *
 * @author zyl
 */
public interface Call<T> {
  Call<T> execute(IActionCallback<T> callback);

  Call<T> enqueue(IActionCallback<T> callback);

  void cancel();
}
