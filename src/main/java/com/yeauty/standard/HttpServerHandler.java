package com.yeauty.standard;

import com.yeauty.pojo.PojoEndpointServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.CharsetUtil;
import org.springframework.util.StringUtils;

import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final PojoEndpointServer pojoEndpointServer;

    public HttpServerHandler(PojoEndpointServer pojoEndpointServer) {
        this.pojoEndpointServer = pojoEndpointServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        handleHttpRequest(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx);
        super.channelInactive(ctx);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (!StringUtils.isEmpty(pojoEndpointServer.getHost()) && !pojoEndpointServer.getHost().equals("0.0.0.0") && !pojoEndpointServer.getHost().equals(req.headers().get(HttpHeaderNames.HOST))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        Set<String> pathSet = pojoEndpointServer.getPathSet();
        if (pathSet != null && pathSet.size() > 0 && !pathSet.contains(req.uri())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Handshake
        Channel channel = ctx.channel();
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + req.headers().get(HOST) + req.uri(), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());
            pipeline.addLast(
                    new WebSocketServerCompressionHandler(),
                    new WebSocketServerHandler(pojoEndpointServer)
            );
            handshaker.handshake(channel, req).addListener(future -> {
                if (future.isSuccess()) {
                    pojoEndpointServer.doOnOpen(ctx, req);
                } else {
                    handshaker.close(channel, new CloseWebSocketFrame());
                }
            });
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
        res.content().writeBytes(buf);
        HttpUtil.setContentLength(res, res.content().readableBytes());

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req)) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}