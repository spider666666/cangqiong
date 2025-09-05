package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> longThreadLocal = new ThreadLocal<>();

    public static ThreadLocal<String>  StringThreadLocal = new ThreadLocal<>();
    //将我们想要存储的数据放在当前线程的局部变量中
    public static void setCurrentId(Long id) {
        longThreadLocal.set(id);
    }

    public static Long getCurrentId() {
        return longThreadLocal.get();
    }
    public static void setUserId(String userId) {
        StringThreadLocal.set(userId);
    }

    public static String getUserId() {
        return StringThreadLocal.get();
    }


}
