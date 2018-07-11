package com.ray.router;

import android.content.Context;
import android.os.Bundle;

import com.ray.router.annotation.Query;
import com.ray.router.annotation.Route;
import com.ray.router.annotation.RouteCtx;
import com.ray.router.facade.Call;

import io.reactivex.Observable;

/**
 * @author zyl
 * @date Created on 2018/2/26
 */
public interface SchoolService {
    @Route("/school")
    Call<Bundle> testActivity(Context context1, @RouteCtx Context context2, @Query("b") boolean b, @Query("school_id") int id);

    @Route("/test")
    Call<String> test(Context context, @Query("b") boolean b, @Query("school_id") int id);

    @Route("/test")
    Call<String> test2(@RouteCtx Context context, @Query("b") boolean b, int id);

    @Route("/school")
    Observable<Bundle> test3(Context context, @Query("b") boolean b, @Query("school_id") int id);
}
