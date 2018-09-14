package com.yeauty.standard;

import com.yeauty.pojo.PojoEndpointServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/25 16:49
 */
public class WebsocketServer {

    private final PojoEndpointServer pojoEndpointServer;

    private final ServerEndpointConfig config;

    public WebsocketServer(PojoEndpointServer webSocketServerHandler, ServerEndpointConfig serverEndpointConfig) {
        this.pojoEndpointServer = webSocketServerHandler;
        this.config = serverEndpointConfig;
    }

    public void init() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,config.getConnectTimeoutMillis())
                .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                .childOption(ChannelOption.WRITE_SPIN_COUNT,config.getWriteSpinCount())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(config.getWriteBufferLowWaterMark(),config.getWriteBufferHighWaterMark()))
                .childOption(ChannelOption.TCP_NODELAY,config.isTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE,config.isSoKeepalive())
                .childOption(ChannelOption.SO_LINGER,config.getSoLinger())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE,config.isAllowHalfClosure())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new HttpServerHandler(pojoEndpointServer));
                    }
                });

        if(config.getSoRcvbuf()!=-1){
            bootstrap.childOption(ChannelOption.SO_RCVBUF,config.getSoRcvbuf());
        }

        if (config.getSoSndbuf()!=-1){
            bootstrap.childOption(ChannelOption.SO_SNDBUF,config.getSoSndbuf());
        }

        if ("0.0.0.0".equals(config.getHost())) {
            bootstrap.bind(config.getPort());
        } else {
            try {
                bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getHost()), config.getPort()));
            } catch (UnknownHostException e) {
                bootstrap.bind(config.getHost(), config.getPort());
                e.printStackTrace();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    public PojoEndpointServer getPojoEndpointServer() {
        return pojoEndpointServer;
    }
}
