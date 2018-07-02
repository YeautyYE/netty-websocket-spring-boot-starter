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
        String host = annotation.host();
        int port = annotation.port();
        boolean tcpNodelay = annotation.tcpNodelay();
        int backlog = annotation.backlog();
        ApplicationContext context = getApplicationContext();
        PojoMethodMapping pojoMethodMapping = new PojoMethodMapping(endpointClass, context);
        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(host, port, path, tcpNodelay, backlog);
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


    public Set<InetSocketAddress> getInetSocketAddressSet() {
        return addressWebsocketServerMap.keySet();
    }

}
