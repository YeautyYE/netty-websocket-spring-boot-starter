package org.yeauty.standard;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;
import org.yeauty.pojo.PojoEndpointServer;
import org.yeauty.support.WsPathMatcher;

import java.io.InputStream;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final PojoEndpointServer pojoEndpointServer;
    private final ServerEndpointConfig config;
    private final EventExecutorGroup eventExecutorGroup;
    private final boolean isCors;

    private static ByteBuf faviconByteBuf = null;
    private static ByteBuf notFoundByteBuf = null;
    private static ByteBuf badRequestByteBuf = null;
    private static ByteBuf forbiddenByteBuf = null;
    private static ByteBuf internalServerErrorByteBuf = null;

    static {
        faviconByteBuf = buildStaticRes("/favicon.ico");
        notFoundByteBuf = buildStaticRes("/public/error/404.html");
        badRequestByteBuf = buildStaticRes("/public/error/400.html");
        forbiddenByteBuf = buildStaticRes("/public/error/403.html");
        internalServerErrorByteBuf = buildStaticRes("/public/error/500.html");
        if (notFoundByteBuf == null) {
            notFoundByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (badRequestByteBuf == null) {
            badRequestByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (forbiddenByteBuf == null) {
            forbiddenByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (internalServerErrorByteBuf == null) {
            internalServerErrorByteBuf = buildStaticRes("/public/error/5xx.html");
        }
    }


    private static ByteBuf buildStaticRes(String resPath) {
        try {
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(resPath);
            if (inputStream != null) {
                int available = inputStream.available();
                if (available != 0) {
                    byte[] bytes = new byte[available];
                    inputStream.read(bytes);
                    return ByteBufAllocator.DEFAULT.buffer(bytes.length).writeBytes(bytes);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public HttpServerHandler(PojoEndpointServer pojoEndpointServer, ServerEndpointConfig config, EventExecutorGroup eventExecutorGroup, boolean isCors) {
        this.pojoEndpointServer = pojoEndpointServer;
        this.config = config;
        this.eventExecutorGroup = eventExecutorGroup;
        this.isCors = isCors;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            handleHttpRequest(ctx, msg);
        } catch (TypeMismatchException e) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        } catch (Exception e) {
            FullHttpResponse res;
            if (internalServerErrorByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, internalServerErrorByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            }
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse res;
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            if (badRequestByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, badRequestByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        HttpHeaders headers = req.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        if (StringUtils.isEmpty(host)) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!StringUtils.isEmpty(pojoEndpointServer.getHost()) && !pojoEndpointServer.getHost().equals("0.0.0.0") && !pojoEndpointServer.getHost().equals(host.split(":")[0])) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();
        if ("/favicon.ico".equals(path)) {
            if (faviconByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, OK, faviconByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        Channel channel = ctx.channel();

        //path match
        String pattern = null;
        Set<WsPathMatcher> pathMatcherSet = pojoEndpointServer.getPathMatcherSet();
        for (WsPathMatcher pathMatcher : pathMatcherSet) {
            if (pathMatcher.matchAndExtract(decoder, channel)) {
                pattern = pathMatcher.getPattern();
                break;
            }
        }

        if (pattern == null) {
            if (notFoundByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, notFoundByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        String subprotocols = null;

        if (pojoEndpointServer.hasBeforeHandshake(channel, pattern)) {
            pojoEndpointServer.doBeforeHandshake(channel, req, pattern);
            if (!channel.isActive()) {
                return;
            }

            AttributeKey<String> subprotocolsAttrKey = AttributeKey.valueOf("subprotocols");
            if (channel.hasAttr(subprotocolsAttrKey)) {
                subprotocols = ctx.channel().attr(subprotocolsAttrKey).get();
            }
        }
        ChannelPipeline pipeline = ctx.pipeline();
        if (config.isUseCompressionHandler()) {
            // Add WebSocketServerCompressionHandler, but don't shake hands
            pipeline.addLast(new WebSocketServerCompressionHandler());
            // Let the request by WebSocketServerCompressionHandler forwarding to the next handler
            ctx.fireChannelRead(req.retain());
        }
        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), subprotocols, true, config.getmaxFramePayloadLength());
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            pipeline.remove(ctx.name());
            if (config.getReaderIdleTimeSeconds() != 0 || config.getWriterIdleTimeSeconds() != 0 || config.getAllIdleTimeSeconds() != 0) {
                pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
            }
            pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
            if (config.isUseEventExecutorGroup()) {
                pipeline.addLast(eventExecutorGroup, new WebSocketServerHandler(pojoEndpointServer));
            } else {
                pipeline.addLast(new WebSocketServerHandler(pojoEndpointServer));
            }
            String finalPattern = pattern;

            String header = headers.get(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
            HttpHeaders httpHeaders = null;
            if (header!=null) {
                httpHeaders = new DefaultHttpHeaders().add(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, header);
            }
            final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req,httpHeaders,ctx.channel().newPromise());

            handshakeFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    if (isCors) {
                        pipeline.remove(CorsHandler.class);
                    }
                    pojoEndpointServer.doOnOpen(channel, req, finalPattern);
                } else {
                    handshaker.close(channel, new CloseWebSocketFrame());
                }
            });
        }

    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        int statusCode = res.status().code();
        if (statusCode != OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        HttpUtil.setContentLength(res, res.content().readableBytes());

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}
