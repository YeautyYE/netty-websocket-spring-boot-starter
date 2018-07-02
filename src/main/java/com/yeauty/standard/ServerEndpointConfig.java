package com.yeauty.standard;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/26 14:16
 */
public class ServerEndpointConfig {

    private final String HOST;
    private final int PORT;
    private final Set<String> PATH_SET;
    private final boolean TCP_NODELAY;
    private final int SO_BACKLOG;
    private static Integer randomPort;

    public ServerEndpointConfig(String host, int port, String path, boolean tcpNodelay, int soBacklog) {
        if (StringUtils.isEmpty(host) || "0.0.0.0".equals(host) || "0.0.0.0/0.0.0.0".equals(host)) {
            this.HOST = "0.0.0.0";
        } else {
            this.HOST = host;
        }
        this.PORT = getAvailablePort(port);
        PATH_SET = new HashSet<>();
        addPath(path);
        this.TCP_NODELAY = tcpNodelay;
        this.SO_BACKLOG = soBacklog;
    }

    public String getHost() {
        return this.HOST;
    }

    public int getPort() {
        return this.PORT;
    }

    public String addPath(String path) {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        if (PATH_SET.contains(path)) {
            throw new RuntimeException("ServerEndpointConfig.addPath  path:" + path + " are repeat.");
        }
        this.PATH_SET.add(path);
        return path;
    }

    public Set<String> getPathSet() {
        return PATH_SET;
    }

    public boolean getTcpNodelay() {
        return TCP_NODELAY;
    }

    public int getSoBacklog() {
        return SO_BACKLOG;
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

}
