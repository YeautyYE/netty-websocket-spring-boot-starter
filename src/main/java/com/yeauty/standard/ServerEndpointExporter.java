package com.yeauty.standard;

import com.yeauty.annotation.ServerEndpoint;
import com.yeauty.pojo.PojoEndpointServer;
import com.yeauty.pojo.PojoMethodMapping;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;

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
            throw new RuntimeException("missingAnnotation ServerEndpoint");
        }
        String path = annotation.value();
        ServerEndpointConfig serverEndpointConfig = buildConfig(annotation);

        ApplicationContext context = getApplicationContext();
        PojoMethodMapping pojoMethodMapping = new PojoMethodMapping(endpointClass, context);

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
        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(host, port, path, optionConnectTimeoutMillis, optionSoBacklog,childOptionWriteSpinCount,childOptionWriteBufferHighWaterMark,childOptionWriteBufferLowWaterMark,childOptionSoRcvbuf,childOptionSoSndbuf,childOptionTcpNodelay,childOptionSoKeepalive,childOptionSoLinger,childOptionAllowHalfClosure);
        return serverEndpointConfig;
    }

    public Set<InetSocketAddress> getInetSocketAddressSet() {
        return addressWebsocketServerMap.keySet();
    }

}
