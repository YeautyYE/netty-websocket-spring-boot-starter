package org.yeauty.standard;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.yeauty.annotation.ServerEndpoint;
import org.yeauty.exception.DeploymentException;
import org.yeauty.pojo.PojoEndpointServer;
import org.yeauty.pojo.PojoMethodMapping;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/25 15:20
 */
public class ServerEndpointExporter extends ApplicationObjectSupport implements SmartInitializingSingleton {

    @Autowired
    Environment environment;

    private final Map<InetSocketAddress, WebsocketServer> addressWebsocketServerMap = new HashMap<>();

    @Override
    public void afterSingletonsInstantiated() {
        registerEndpoints();
    }

    protected void registerEndpoints() {
        Set<Class<?>> endpointClasses = new LinkedHashSet<>();

        ApplicationContext context = getApplicationContext();
        if (context != null) {
            String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
            for (String beanName : endpointBeanNames) {
                endpointClasses.add(context.getType(beanName));
            }
        }

        for (Class<?> endpointClass : endpointClasses) {
            registerEndpoint(endpointClass);
        }

        init();
    }

    private void init() {
        for (Map.Entry<InetSocketAddress, WebsocketServer> entry : addressWebsocketServerMap.entrySet()) {
            WebsocketServer websocketServer = entry.getValue();
            try {
                websocketServer.init();
            } catch (InterruptedException e) {
                logger.error("websocket [" + entry.getKey() + "] init fail", e);
            }
        }
    }

    private void registerEndpoint(Class<?> endpointClass) {
        ServerEndpoint annotation = AnnotationUtils.findAnnotation(endpointClass, ServerEndpoint.class);
        if (annotation == null) {
            throw new IllegalStateException("missingAnnotation ServerEndpoint");
        }
        String path = annotation.value();
        ServerEndpointConfig serverEndpointConfig = buildConfig(annotation);

        ApplicationContext context = getApplicationContext();
        PojoMethodMapping pojoMethodMapping = null;
        try {
            pojoMethodMapping = new PojoMethodMapping(endpointClass, context);
        } catch (DeploymentException e) {
            throw new IllegalStateException("Failed to register ServerEndpointConfig: " + serverEndpointConfig, e);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverEndpointConfig.getHost(), serverEndpointConfig.getPort());
        WebsocketServer websocketServer = addressWebsocketServerMap.get(inetSocketAddress);
        if (websocketServer == null) {
            PojoEndpointServer pojoEndpointServer = new PojoEndpointServer(pojoMethodMapping, serverEndpointConfig);
            websocketServer = new WebsocketServer(pojoEndpointServer, serverEndpointConfig);
            addressWebsocketServerMap.put(inetSocketAddress, websocketServer);

        } else {
            websocketServer.getPojoEndpointServer().addPathPojoMethodMapping(path, pojoMethodMapping);
        }
    }

    private ServerEndpointConfig buildConfig(ServerEndpoint annotation) {
        String host = annotation.host();
        int port = annotation.port();
        String path = annotation.value();

        int optionConnectTimeoutMillis = annotation.optionConnectTimeoutMillis();
        int optionSoBacklog = annotation.optionSoBacklog();

        int childOptionWriteSpinCount = annotation.childOptionWriteSpinCount();
        int childOptionWriteBufferHighWaterMark = annotation.childOptionWriteBufferHighWaterMark();
        int childOptionWriteBufferLowWaterMark = annotation.childOptionWriteBufferLowWaterMark();
        int childOptionSoRcvbuf = annotation.childOptionSoRcvbuf();
        int childOptionSoSndbuf = annotation.childOptionSoSndbuf();
        boolean childOptionTcpNodelay = annotation.childOptionTcpNodelay();
        boolean childOptionSoKeepalive = annotation.childOptionSoKeepalive();
        int childOptionSoLinger = annotation.childOptionSoLinger();
        boolean childOptionAllowHalfClosure = annotation.childOptionAllowHalfClosure();

        int readerIdleTimeSeconds = annotation.readerIdleTimeSeconds();
        int writerIdleTimeSeconds = annotation.writerIdleTimeSeconds();
        int allIdleTimeSeconds = annotation.allIdleTimeSeconds();

        String prefix = annotation.prefix();
        if (!StringUtils.isEmpty(prefix)) {
            String hostFromEnv = environment.getProperty(prefix + ".host", String.class);
            if (hostFromEnv != null) {
                host = hostFromEnv;
            }
            Integer portFromEnv = environment.getProperty(prefix + ".port", Integer.class);
            if (portFromEnv != null) {
                port = portFromEnv;
            }
            String pathFromEnv = environment.getProperty(prefix + ".path", String.class);
            if (pathFromEnv != null) {
                path = pathFromEnv;
            }
            Integer optionConnectTimeoutMillisFromEnv = environment.getProperty(prefix + ".option.connect-timeout-millis", Integer.class);
            if (optionConnectTimeoutMillisFromEnv != null) {
                optionConnectTimeoutMillis = optionConnectTimeoutMillisFromEnv;
            }
            Integer optionSoBacklogFromEnv = environment.getProperty(prefix + ".option.so-backlog", Integer.class);
            if (optionSoBacklogFromEnv != null) {
                optionSoBacklog = optionSoBacklogFromEnv;
            }
            Integer childOptionWriteSpinCountFromEnv = environment.getProperty(prefix + ".child-option.write-spin-count", Integer.class);
            if (childOptionWriteSpinCountFromEnv != null) {
                childOptionWriteSpinCount = childOptionWriteSpinCountFromEnv;
            }
            Integer childOptionWriteBufferHighWaterMarkFromEnv = environment.getProperty(prefix + ".child-option.write-buffer-high-water-mark", Integer.class);
            if (childOptionWriteBufferHighWaterMarkFromEnv != null) {
                childOptionWriteBufferHighWaterMark = childOptionWriteBufferHighWaterMarkFromEnv;
            }
            Integer childOptionWriteBufferLowWaterMarkFromEnv = environment.getProperty(prefix + ".child-option.write-buffer-low-water-mark", Integer.class);
            if (childOptionWriteBufferLowWaterMarkFromEnv != null) {
                childOptionWriteBufferLowWaterMark = childOptionWriteBufferLowWaterMarkFromEnv;
            }
            Integer childOptionSoRcvbufFromEnv = environment.getProperty(prefix + ".child-option.so-rcvbuf", Integer.class);
            if (childOptionSoRcvbufFromEnv != null) {
                childOptionSoRcvbuf = childOptionSoRcvbufFromEnv;
            }
            Integer childOptionSoSndbufFromEnv = environment.getProperty(prefix + ".child-option.so-sndbuf", Integer.class);
            if (childOptionSoSndbufFromEnv != null) {
                childOptionSoSndbuf = childOptionSoSndbufFromEnv;
            }
            Boolean childOptionTcpNodelayFromEnv = environment.getProperty(prefix + ".child-option.tcp-nodelay", Boolean.class);
            if (childOptionTcpNodelayFromEnv != null) {
                childOptionTcpNodelay = childOptionTcpNodelayFromEnv;
            }
            Boolean childOptionSoKeepaliveFromEnv = environment.getProperty(prefix + ".child-option.so-keepalive", Boolean.class);
            if (childOptionSoKeepaliveFromEnv != null) {
                childOptionSoKeepalive = childOptionSoKeepaliveFromEnv;
            }
            Integer childOptionSoLingerFromEnv = environment.getProperty(prefix + ".child-option.so-linger", Integer.class);
            if (childOptionSoLingerFromEnv != null) {
                childOptionSoLinger = childOptionSoLingerFromEnv;
            }
            Boolean childOptionAllowHalfClosureFromEnv = environment.getProperty(prefix + ".child-option.allow-half-closure", Boolean.class);
            if (childOptionAllowHalfClosureFromEnv != null) {
                childOptionAllowHalfClosure = childOptionAllowHalfClosureFromEnv;
            }
            Integer readerIdleTimeSecondsFromEnv = environment.getProperty(prefix + ".reader-idle-time-seconds", Integer.class);
            if (readerIdleTimeSecondsFromEnv != null) {
                readerIdleTimeSeconds = readerIdleTimeSecondsFromEnv;
            }
            Integer writerIdleTimeSecondsFromEnv = environment.getProperty(prefix + ".writer-idle-time-seconds", Integer.class);
            if (writerIdleTimeSecondsFromEnv != null) {
                writerIdleTimeSeconds = writerIdleTimeSecondsFromEnv;
            }
            Integer allIdleTimeSecondsFromEnv = environment.getProperty(prefix + ".all-idle-time-seconds", Integer.class);
            if (allIdleTimeSecondsFromEnv != null) {
                allIdleTimeSeconds = allIdleTimeSecondsFromEnv;
            }
        }

        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(host, port, path, optionConnectTimeoutMillis, optionSoBacklog, childOptionWriteSpinCount, childOptionWriteBufferHighWaterMark, childOptionWriteBufferLowWaterMark, childOptionSoRcvbuf, childOptionSoSndbuf, childOptionTcpNodelay, childOptionSoKeepalive, childOptionSoLinger, childOptionAllowHalfClosure, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
        return serverEndpointConfig;
    }

    public Set<InetSocketAddress> getInetSocketAddressSet() {
        return addressWebsocketServerMap.keySet();
    }

}
