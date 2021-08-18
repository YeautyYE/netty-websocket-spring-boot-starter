package org.yeauty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yeauty
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEndpoint {

    @AliasFor("path")
    String value() default "/";

    @AliasFor("value")
    String path() default "/";

    String host() default "0.0.0.0";

    String port() default "80";

    String bossLoopGroupThreads() default "1";

    String workerLoopGroupThreads() default "0";

    String useCompressionHandler() default "false";

    //------------------------- option -------------------------

    String optionConnectTimeoutMillis() default "30000";

    String optionSoBacklog() default "128";

    //------------------------- childOption -------------------------

    String childOptionWriteSpinCount() default "16";

    String childOptionWriteBufferHighWaterMark() default "65536";

    String childOptionWriteBufferLowWaterMark() default "32768";

    String childOptionSoRcvbuf() default "-1";

    String childOptionSoSndbuf() default "-1";

    String childOptionTcpNodelay() default "true";

    String childOptionSoKeepalive() default "false";

    String childOptionSoLinger() default "-1";

    String childOptionAllowHalfClosure() default "false";

    //------------------------- idleEvent -------------------------

    String readerIdleTimeSeconds() default "0";

    String writerIdleTimeSeconds() default "0";

    String allIdleTimeSeconds() default "0";

    //------------------------- handshake -------------------------

    String maxFramePayloadLength() default "65536";

    //------------------------- eventExecutorGroup -------------------------

    String useEventExecutorGroup() default "true"; //use EventExecutorGroup(another thread pool) to perform time-consuming synchronous business logic

    String eventExecutorGroupThreads() default "16";

    //------------------------- ssl (refer to spring Ssl) -------------------------

    /**
     * {@link org.springframework.boot.web.server.Ssl}
     */

    String sslKeyPassword() default "";

    String sslKeyStore() default "";            //e.g. classpath:server.jks

    String sslKeyStorePassword() default "";

    String sslKeyStoreType() default "";        //e.g. JKS

    String sslTrustStore() default "";

    String sslTrustStorePassword() default "";

    String sslTrustStoreType() default "";

    //------------------------- cors (refer to spring CrossOrigin) -------------------------

    /**
     * {@link org.springframework.web.bind.annotation.CrossOrigin}
     */

    String[] corsOrigins() default {};

    String corsAllowCredentials() default "";


}
