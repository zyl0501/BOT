# BOT
Router Framework

#### 使用方式
1. 添加依赖和配置
``` gradle
android {
    defaultConfig {
	...
	javaCompileOptions {
	    annotationProcessorOptions {
		arguments = [ moduleName : project.getName() ]
	    }
	}
    }
}

dependencies {
    implementation 'com.ray:router:1.0.12'
    annotationProcessor 'com.ray:router-compiler:1.0.12'
    ...
}
```

使用 Gradle 插件实现路由表的自动加载
```
    apply plugin: 'com.ray.router'
    
    buildscript {
        repositories {
            jcenter()
        }
    
        dependencies {
            classpath "com.ray:router-gradle-plugin:1.0.12"
        }
    }
```

2. 配置Action
``` java
    @Action(path = "/test/activity")
    public class MyActivity extend Activity {
        ...
    }
    @Action(path = "/test/fragment")
    public class MyFragment extend Activity {
        ...
    }
    @Action(path = "/test/action")
    public class MyAction implete IAction {
        ...
    }
```

3.发起路由操作
``` java
    //这个Provider由注解生成，命名规则Router[gradle中配置的moduleName]Provider
    Router.I().registerProvider(new RouterTestModuleProvider());
    Router.I().bundle("/test")
            .withBoolean("b", true)
            .withInt("i", 1)
            .dispatch(MainActivity.this, Void.class, new IActionCallback<Bundle>() {
                @Override
                public void onIntercept(Request request) {
                    //被拦截时触发
                }

                @Override
                public void onResponse(Response<Bundle> response) {
                    if (response.getCode() == Response.CODE_SUCCESS) {
		    	//成功返回
                        Bundle data = response.getData();
                    } else {
		    	//失败
                    }
                }

                @Override
                public void onException(Throwable t) {
                    //出错
                }

                @Override
                public void onCancel() {
                    //取消
                }
            });
```

4.配置拦截器Interceptor（可选）
``` java
    全局
    @Interceptor
    public class MyInterceptor implements IInterceptor {
        ...
    }
```
``` java
    特定路径
    @Interceptor(path="/test/activity")
    public class MyInterceptor implements IInterceptor {
        ...
    }
```
``` java
    正则匹配
    @Interceptor(pattern="/test/*")
    public class MyInterceptor implements IInterceptor {
        ...
    }
```
``` java
    Action下的拦截器
    @Action(path = "/test/action")
    @Interceptor(clz = MyInterceptor.class)
    public class MyAction implete IAction {
        ...
    }
```

5.添加混淆规则(如果使用了Proguard)
``` 
-keep public class com.ray.router.**{*;}
-keep class * implements com.ray.router.facade.ISyringe{*;}
```

6.可以使用注册 Service 的方式
``` java
    public interface TestService {
        @Route("/school")
        Call<Bundle> testActivity(Context context1, @RouteCtx Context context2, @Query("b") boolean b, @Query("school_id") int id);
    
        @Route("/test")
        Call<String> test(Context context, @Query("b") boolean b, @Query("school_id") int id);
    
        @Route("/test")
        Call<String> test2(@RouteCtx Context context, @Query("b") boolean b, int id);
    
        //Rx 模式，需要在项目 module 的 build.gradle 中增加依赖
        //implementation 'com.ray:router-rx:1.0.12'
        @Route("/school")
        Observable<Bundle> test3(Context context, @Query("b") boolean b, @Query("school_id") int id);
    }
```
``` java
	Router.I().service(TestService.class)
		  .test()
		  ...
```
