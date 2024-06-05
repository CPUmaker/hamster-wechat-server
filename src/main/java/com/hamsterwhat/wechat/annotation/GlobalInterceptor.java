package com.hamsterwhat.wechat.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalInterceptor {
    /**
     * Set to true when the interceptor need to check whether user is logged in or not
     */
    boolean checkLogin() default true;

    /**
     * Set to true when the interceptor need to check whether user is admin or not
     */
    boolean checkAdmin() default false;
}
