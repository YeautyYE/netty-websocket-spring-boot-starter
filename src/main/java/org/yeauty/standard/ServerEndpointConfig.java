package org.yeauty.standard;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Yeauty
 * @version 1.0
 */
public class ServerEndpointConfig {

    private final String HOST;
    private final int PORT;
    private final int BOSS_LOOP_GROUP_THREADS;
    private final int WORKER_LOOP_GROUP_THREADS;
    private final boolean USE_COMPRESSION_HANDLER;
    private final int CONNECT_TIMEOUT_MILLIS;
    private final int SO_BACKLOG;
    private final int WRITE_SPIN_COUNT;
    private final int WRITE_BUFFER_HIGH_WATER_MARK;
    private final int WRITE_BUFFER_LOW_WATER_MARK;
    private final int SO_RCVBUF;
    private final int SO_SNDBUF;
    private final boolean TCP_NODELAY;
    private final boolean SO_KEEPALIVE;
    private final int SO_LINGER;
    private final boolean ALLOW_HALF_CLOSURE;
    private final int READER_IDLE_TIME_SECONDS;
    private final int WRITER_IDLE_TIME_SECONDS;
    private final int ALL_IDLE_TIME_SECONDS;
    private final int MAX_FRAME_PAYLOAD_LENGTH;
    private final boolean USE_EVENT_EXECUTOR_GROUP;
    private final int EVENT_EXECUTOR_GROUP_THREADS;

    private final String KEY_PASSWORD;
    private final String KEY_STORE;
    private final String KEY_STORE_PASSWORD;
    private final String KEY_STORE_TYPE;
    private final String TRUST_STORE;
    private final String TRUST_STORE_PASSWORD;
    private final String TRUST_STORE_TYPE;

    private final String[] CORS_ORIGINS;
    private final Boolean CORS_ALLOW_CREDENTIALS;

    private static Integer randomPort;

    public ServerEndpointConfig(String host, int port, int bossLoopGroupThreads, int workerLoopGroupThreads, boolean useCompressionHandler, int connectTimeoutMillis, int soBacklog, int writeSpinCount, int writeBufferHighWaterMark, int writeBufferLowWaterMark, int soRcvbuf, int soSndbuf, boolean tcpNodelay, boolean soKeepalive, int soLinger, boolean allowHalfClosure, int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, int maxFramePayloadLength, boolean useEventExecutorGroup, int eventExecutorGroupThreads, String keyPassword, String keyStore, String keyStorePassword, String keyStoreType, String trustStore, String trustStorePassword, String trustStoreType, String[] corsOrigins, Boolean corsAllowCredentials) {
        if (StringUtils.isEmpty(host) || "0.0.0.0".equals(host) || "0.0.0.0/0.0.0.0".equals(host)) {
            this.HOST = "0.0.0.0";
        } else {
            this.HOST = host;
        }
        this.PORT = getAvailablePort(port);
        this.BOSS_LOOP_GROUP_THREADS = bossLoopGroupThreads;
        this.WORKER_LOOP_GROUP_THREADS = workerLoopGroupThreads;
        this.USE_COMPRESSION_HANDLER = useCompressionHandler;
        this.CONNECT_TIMEOUT_MILLIS = connectTimeoutMillis;
        this.SO_BACKLOG = soBacklog;
        this.WRITE_SPIN_COUNT = writeSpinCount;
        this.WRITE_BUFFER_HIGH_WATER_MARK = writeBufferHighWaterMark;
        this.WRITE_BUFFER_LOW_WATER_MARK = writeBufferLowWaterMark;
        this.SO_RCVBUF = soRcvbuf;
        this.SO_SNDBUF = soSndbuf;
        this.TCP_NODELAY = tcpNodelay;
        this.SO_KEEPALIVE = soKeepalive;
        this.SO_LINGER = soLinger;
        this.ALLOW_HALF_CLOSURE = allowHalfClosure;
        this.READER_IDLE_TIME_SECONDS = readerIdleTimeSeconds;
        this.WRITER_IDLE_TIME_SECONDS = writerIdleTimeSeconds;
        this.ALL_IDLE_TIME_SECONDS = allIdleTimeSeconds;
        this.MAX_FRAME_PAYLOAD_LENGTH = maxFramePayloadLength;
        this.USE_EVENT_EXECUTOR_GROUP = useEventExecutorGroup;
        this.EVENT_EXECUTOR_GROUP_THREADS = eventExecutorGroupThreads;

        this.KEY_PASSWORD = keyPassword;
        this.KEY_STORE = keyStore;
        this.KEY_STORE_PASSWORD = keyStorePassword;
        this.KEY_STORE_TYPE = keyStoreType;
        this.TRUST_STORE = trustStore;
        this.TRUST_STORE_PASSWORD = trustStorePassword;
        this.TRUST_STORE_TYPE = trustStoreType;

        this.CORS_ORIGINS = corsOrigins;
        this.CORS_ALLOW_CREDENTIALS = corsAllowCredentials;
    }

    private int getAvailablePort(int port) {
        if (port != 0) {
            return port;
        }
        if (randomPort != null && randomPort != 0) {
            return randomPort;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
        Socket socket = new Socket();
        try {
            socket.bind(inetSocketAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int localPort = socket.getLocalPort();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        randomPort = localPort;
        return localPort;
    }

    public String getHost() {
        return HOST;
    }

    public int getPort() {
        return PORT;
    }

    /*public Set<String> getPathSet() {

        return PATH_SET;
    }*/

    public int getBossLoopGroupThreads() {
        return BOSS_LOOP_GROUP_THREADS;
    }

    public int getWorkerLoopGroupThreads() {
        return WORKER_LOOP_GROUP_THREADS;
    }

    public boolean isUseCompressionHandler() {
        return USE_COMPRESSION_HANDLER;
    }

    public int getConnectTimeoutMillis() {
        return CONNECT_TIMEOUT_MILLIS;
    }

    public int getSoBacklog() {
        return SO_BACKLOG;
    }

    public int getWriteSpinCount() {
        return WRITE_SPIN_COUNT;
    }

    public int getWriteBufferHighWaterMark() {
        return WRITE_BUFFER_HIGH_WATER_MARK;
    }

    public int getWriteBufferLowWaterMark() {
        return WRITE_BUFFER_LOW_WATER_MARK;
    }

    public int getSoRcvbuf() {
        return SO_RCVBUF;
    }

    public int getSoSndbuf() {
        return SO_SNDBUF;
    }

    public boolean isTcpNodelay() {
        return TCP_NODELAY;
    }

    public boolean isSoKeepalive() {
        return SO_KEEPALIVE;
    }

    public int getSoLinger() {
        return SO_LINGER;
    }

    public boolean isAllowHalfClosure() {
        return ALLOW_HALF_CLOSURE;
    }

    public static Integer getRandomPort() {
        return randomPort;
    }

    public int getReaderIdleTimeSeconds() {
        return READER_IDLE_TIME_SECONDS;
    }

    public int getWriterIdleTimeSeconds() {
        return WRITER_IDLE_TIME_SECONDS;
    }

    public int getAllIdleTimeSeconds() {
        return ALL_IDLE_TIME_SECONDS;
    }

    public int getmaxFramePayloadLength() {
        return MAX_FRAME_PAYLOAD_LENGTH;
    }

    public boolean isUseEventExecutorGroup() {
        return USE_EVENT_EXECUTOR_GROUP;
    }

    public int getEventExecutorGroupThreads() {
        return EVENT_EXECUTOR_GROUP_THREADS;
    }

    public String getKeyPassword() {
        return KEY_PASSWORD;
    }

    public String getKeyStore() {
        return KEY_STORE;
    }

    public String getKeyStorePassword() {
        return KEY_STORE_PASSWORD;
    }

    public String getKeyStoreType() {
        return KEY_STORE_TYPE;
    }

    public String getTrustStore() {
        return TRUST_STORE;
    }

    public String getTrustStorePassword() {
        return TRUST_STORE_PASSWORD;
    }

    public String getTrustStoreType() {
        return TRUST_STORE_TYPE;
    }

    public String[] getCorsOrigins() {
        return CORS_ORIGINS;
    }

    public Boolean getCorsAllowCredentials() {
        return CORS_ALLOW_CREDENTIALS;
    }
}
