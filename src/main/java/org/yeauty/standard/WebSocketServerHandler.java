package org.yeauty.standard;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.yeauty.pojo.PojoEndpointServer;

class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final PojoEndpointServer pojoEndpointServer;

    public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer) {
        this.pojoEndpointServer = pojoEndpointServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handleWebSocketFrame(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        pojoEndpointServer.doOnEvent(ctx.channel(), evt);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            pojoEndpointServer.doOnMessage(ctx.channel(), frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            pojoEndpointServer.doOnBinary(ctx.channel(), frame);
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            return;
        }
    }

}