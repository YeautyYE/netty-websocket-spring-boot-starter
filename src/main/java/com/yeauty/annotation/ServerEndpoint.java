package com.yeauty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/25 15:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEndpoint {

    @AliasFor("path")
    String value() default "/";

    @AliasFor("value")
    String path() default "/";

    String host() default "0.0.0.0";

    int port() default 80;

    //------------------------- option -------------------------

    int optionConnectTimeoutMillis() default 30000;

    int optionSoBacklog() default 128;

    //------------------------- childOption -------------------------

    int childOptionWriteSpinCount() default 16;

    int childOptionWriteBufferHighWaterMark() default 64 * 1024;

    int childOptionWriteBufferLowWaterMark() default 32 * 1024;

    int childOptionSoRcvbuf() default -1;

    int childOptionSoSndbuf() default -1;

    boolean childOptionTcpNodelay() default true;

    boolean childOptionSoKeepalive() default false;

    int childOptionSoLinger() default -1;

    boolean childOptionAllowHalfClosure() default false;

}
