package org.yeauty.standard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.util.StringUtils;
import org.yeauty.pojo.PojoEndpointServer;
import org.yeauty.util.SslUtils;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author Yeauty
 * @version 1.0
 */
public class WebsocketServer {

    private final PojoEndpointServer pojoEndpointServer;

    private final ServerEndpointConfig config;


    private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebsocketServer.class);

    public WebsocketServer(PojoEndpointServer webSocketServerHandler, ServerEndpointConfig serverEndpointConfig) {
        this.pojoEndpointServer = webSocketServerHandler;
        this.config = serverEndpointConfig;

    }

    public void init() throws InterruptedException, SSLException {
        EventExecutorGroup eventExecutorGroup = null;
        final SslContext sslCtx;
        if (!StringUtils.isEmpty(config.getKeyStore())) {
            sslCtx = SslUtils.createSslContext(config.getKeyPassword(), config.getKeyStore(), config.getKeyStoreType(), config.getKeyStorePassword(), config.getTrustStore(), config.getTrustStoreType(), config.getTrustStorePassword());
        } else {
            sslCtx = null;
        }
        String[] corsOrigins = config.getCorsOrigins();
        Boolean corsAllowCredentials = config.getCorsAllowCredentials();
        final CorsConfig corsConfig = createCorsConfig(corsOrigins, corsAllowCredentials);

        if (config.isUseEventExecutorGroup()) {
            eventExecutorGroup = new DefaultEventExecutorGroup(config.getEventExecutorGroupThreads() == 0 ? 16 : config.getEventExecutorGroupThreads());
        }
        EventLoopGroup boss = new NioEventLoopGroup(config.getBossLoopGroupThreads());
        EventLoopGroup worker = new NioEventLoopGroup(config.getWorkerLoopGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventExecutorGroup finalEventExecutorGroup = eventExecutorGroup;
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                .childOption(ChannelOption.WRITE_SPIN_COUNT, config.getWriteSpinCount())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(config.getWriteBufferLowWaterMark(), config.getWriteBufferHighWaterMark()))
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive())
                .childOption(ChannelOption.SO_LINGER, config.getSoLinger())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, config.isAllowHalfClosure())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslCtx != null) {
                            pipeline.addFirst(sslCtx.newHandler(ch.alloc()));
                        }
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        if (corsConfig != null) {
                            pipeline.addLast(new CorsHandler(corsConfig));
                        }
                        pipeline.addLast(new HttpServerHandler(pojoEndpointServer, config, finalEventExecutorGroup, corsConfig != null));
                    }
                });

        if (config.getSoRcvbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getSoRcvbuf());
        }

        if (config.getSoSndbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getSoSndbuf());
        }

        ChannelFuture channelFuture;
        if ("0.0.0.0".equals(config.getHost())) {
            channelFuture = bootstrap.bind(config.getPort());
        } else {
            try {
                channelFuture = bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getHost()), config.getPort()));
            } catch (UnknownHostException e) {
                channelFuture = bootstrap.bind(config.getHost(), config.getPort());
                e.printStackTrace();
            }
        }

        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    private CorsConfig createCorsConfig(String[] corsOrigins, Boolean corsAllowCredentials) {
        if (corsOrigins.length == 0) {
            return null;
        }
        CorsConfigBuilder corsConfigBuilder = null;
        for (String corsOrigin : corsOrigins) {
            if ("*".equals(corsOrigin)) {
                corsConfigBuilder = CorsConfigBuilder.forAnyOrigin();
                break;
            }
        }
        if (corsConfigBuilder == null) {
            corsConfigBuilder = CorsConfigBuilder.forOrigins(corsOrigins);
        }
        if (corsAllowCredentials != null && corsAllowCredentials) {
            corsConfigBuilder.allowCredentials();
        }
        corsConfigBuilder.allowNullOrigin();
        return corsConfigBuilder.build();
    }

    public PojoEndpointServer getPojoEndpointServer() {
        return pojoEndpointServer;
    }
}
