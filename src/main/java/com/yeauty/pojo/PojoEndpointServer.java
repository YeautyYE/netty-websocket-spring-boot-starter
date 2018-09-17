package com.yeauty.pojo;

import com.yeauty.standard.ServerEndpointConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/26 13:43
 */
public class PojoEndpointServer {

    private static final AttributeKey POJO_KEY = AttributeKey.valueOf("WEBSOCKET_IMPLEMENT");

    private static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("WEBSOCKET_SESSION");

    private static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("WEBSOCKET_PATH");

    private final Map<String, PojoMethodMapping> pathMethodMappingMap = new HashMap<>();

    private final ServerEndpointConfig config;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PojoEndpointServer.class);

    public PojoEndpointServer(PojoMethodMapping methodMapping, ServerEndpointConfig config) {
        pathMethodMappingMap.put(config.getPathSet().iterator().next(), methodMapping);
        this.config = config;
    }

    public void doOnOpen(ChannelHandlerContext ctx, FullHttpRequest req) {
        Channel channel = ctx.channel();
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            Attribute<String> attrPath = channel.attr(PATH_KEY);
            String path = req.uri();
            attrPath.set(req.uri());
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                throw new RuntimeException("path " + path + " is not in pathMethodMappingMap ");
            }
        }
        Attribute attrPojo = channel.attr(POJO_KEY);
        Object implement = null;
        try {
            implement = methodMapping.getEndpointInstance();
            attrPojo.set(implement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Attribute<Session> attrSession = channel.attr(SESSION_KEY);
        Session session = null;
        try {
            session = new Session(channel);
            attrSession.set(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpHeaders headers = req.headers();
        if (methodMapping.getOnOpen() != null) {
            try {
                methodMapping.getOnOpen().invoke(implement, methodMapping.getOnOpenArgs(session, headers));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    public void doOnClose(ChannelHandlerContext ctx) {
        Attribute<String> attrPath = ctx.channel().attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                return;
            }
        }
        if (methodMapping.getOnClose() != null) {
            Object implement = ctx.channel().attr(POJO_KEY).get();
            Session session = ctx.channel().attr(SESSION_KEY).get();
            if (implement == null || session == null) {
                return;
            }
            try {
                methodMapping.getOnClose().invoke(implement,
                        methodMapping.getOnCloseArgs(session));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }


    public void doOnError(ChannelHandlerContext ctx, Throwable throwable) {
        Attribute<String> attrPath = ctx.channel().attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnError() != null) {
            Object implement = ctx.channel().attr(POJO_KEY).get();
            Session session = ctx.channel().attr(SESSION_KEY).get();
            if (session==null||implement==null){
                logger.error(throwable);
            }
            try {
                methodMapping.getOnError().invoke(implement, methodMapping.getOnErrorArgs(session, throwable));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    public void doOnMessage(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Attribute<String> attrPath = ctx.channel().attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnMessage() != null) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            Object implement = ctx.channel().attr(POJO_KEY).get();
            Session session = ctx.channel().attr(SESSION_KEY).get();
            try {
                methodMapping.getOnMessage().invoke(implement, methodMapping.getOnMessageArgs(session, textFrame.text()));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    public void doOnBinary(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Attribute<String> attrPath = ctx.channel().attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnBinary() != null) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            ByteBuf content = binaryWebSocketFrame.content();
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            Object implement = ctx.channel().attr(POJO_KEY).get();
            Session session = ctx.channel().attr(SESSION_KEY).get();
            try {
                methodMapping.getOnBinary().invoke(implement, methodMapping.getOnBinaryArgs(session, bytes));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    public void doOnEvent(ChannelHandlerContext ctx, Object evt) {
        Attribute<String> attrPath = ctx.channel().attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnEvent() != null) {
            Object implement = ctx.channel().attr(POJO_KEY).get();
            Session session = ctx.channel().attr(SESSION_KEY).get();
            try {
                methodMapping.getOnEvent().invoke(implement, methodMapping.getOnEventArgs(session, evt));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    public String getHost() {
        return config.getHost();
    }

    public Set<String> getPathSet() {
        return config.getPathSet();
    }

    public void addPathPojoMethodMapping(String path, PojoMethodMapping pojoMethodMapping) {
        path = config.addPath(path);
        pathMethodMappingMap.put(path, pojoMethodMapping);
    }

}
