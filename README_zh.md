netty-websocket-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[English](https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README.md)

### 简介
本项目帮助你在spring-boot中使用Netty来开发WebSocket服务器，并像spring-websocket的注解开发一样简单

### 要求
- jdk版本为1.8或者1.8+


### 快速开始
- clone本项目到本地，并安装到本地Maven仓库

```
mvn clean install
```

- 添加依赖:

```xml
	<dependency>
		<groupId>com.yeauty</groupId>
		<artifactId>netty-websocket-spring-boot-starter</artifactId>
		<version>0.5.2-SNAPSHOT</version>
	</dependency>
```

- new一个`ServerEndpointExporter`对象，交给Spring容器，表示要开启WebSocket功能，样例如下:

```java
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

- 在端点类上加上`@ServerEndpoint`、`@Component`注解，并在相应的方法上加上`@OnOpen`、`@OnClose`、`@OnError`、`@OnMessage`、`@OnBinary`注解，样例如下：

```java
@ServerEndpoint
@Component
public class MyWebSocket {

    @OnOpen
    public void onOpen(Session session, HttpHeaders headers) throws IOException {
        System.out.println("new connection");
    }

    @OnClose
    public void onClose(Session session) throws IOException {
       System.out.println("one connection closed"); 
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
        System.out.println(text);
        session.sendText("Hello Netty!");
    }

    @OnBinary
    public void OnBinary(Session session, byte[] bytes) {
        for (byte b : bytes) {
            System.out.println(b);
        }
        session.sendBinary(bytes); 
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println(“read idle”);
                    break;
                case WRITER_IDLE:
                    System.out.println(“write idle”);
                    break;
                case ALL_IDLE:
                    System.out.println(“all idle”);
                    break;
                default:
                    break;
            }
        }
    }

}
```

- 打开WebSocket客户端，连接到`ws://127.0.0.1:80`


### 注解
###### @ServerEndpoint 
> 当ServerEndpointExporter类通过Spring配置进行声明并被使用，它将会去扫描带有@ServerEndpoint注解的类
> 被注解的类将被注册成为一个WebSocket端点
> 所有的[配置项](#%E9%85%8D%E7%BD%AE)都在这个注解的属性中 ( 如:`@ServerEndpoint("/ws")` )

###### @OnOpen 
> 当有新的WebSocket连接进入时，对该方法进行回调
> 注入参数的类型:Session、HttpHeaders

###### @OnClose
> 当有WebSocket连接关闭时，对该方法进行回调
> 注入参数的类型:Session

###### @OnError
> 当有WebSocket抛出异常时，对该方法进行回调
> 注入参数的类型:Session、Throwable

###### @OnMessage
> 当接收到字符串消息时，对该方法进行回调
> 注入参数的类型:Session、String

###### @OnBinary
> 当接收到二进制消息时，对该方法进行回调
> 注入参数的类型:Session、byte[]

### 配置
> 所有的配置项都在这个注解的属性中

| 属性  | 默认值 | 说明 
|---|---|---
|path|"/"|WebSocket的path,也可以用`value`来设置
|host|"0.0.0.0"|WebSocket的host,`"0.0.0.0"`即是所有本地地址
|port|80|WebSocket绑定端口号。如果为0，则使用随机端口(端口获取可见 [多端点服务](#%E5%A4%9A%E7%AB%AF%E7%82%B9%E6%9C%8D%E5%8A%A1))
|optionConnectTimeoutMillis|30000|与Netty的`ChannelOption.CONNECT_TIMEOUT_MILLIS`一致
|optionSoBacklog|128|与Netty的`ChannelOption.SO_BACKLOG`一致
|childOptionWriteSpinCount|16|与Netty的`ChannelOption.WRITE_SPIN_COUNT`一致
|childOptionWriteBufferHighWaterMark|64*1024|与Netty的`ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK`一致,但实际上是使用`ChannelOption.WRITE_BUFFER_WATER_MARK`
|childOptionWriteBufferLowWaterMark|32*1024|与Netty的`ChannelOption.WRITE_BUFFER_LOW_WATER_MARK`一致,但实际上是使用 `ChannelOption.WRITE_BUFFER_WATER_MARK`
|childOptionSoRcvbuf|-1(即未设置)|与Netty的`ChannelOption.SO_RCVBUF`一致
|childOptionSoSndbuf|-1(即未设置)|与Netty的`ChannelOption.SO_SNDBUF`一致
|childOptionTcpNodelay|true|与Netty的`ChannelOption.TCP_NODELAY`一致
|childOptionSoKeepalive|false|与Netty的`ChannelOption.SO_KEEPALIVE`一致
|childOptionSoLinger|-1|与Netty的`ChannelOption.SO_LINGER`一致
|childOptionAllowHalfClosure|false|与Netty的`ChannelOption.ALLOW_HALF_CLOSURE`一致
|readerIdleTimeSeconds|0|与`IdleStateHandler`中的`readerIdleTimeSeconds`一致，并且当它不为0时，将在`pipeline`中添加`IdleStateHandler`
|writerIdleTimeSeconds|0|与`IdleStateHandler`中的`writerIdleTimeSeconds`一致，并且当它不为0时，将在`pipeline`中添加`IdleStateHandler`
|allIdleTimeSeconds|0|与`IdleStateHandler`中的`allIdleTimeSeconds`一致，并且当它不为0时，将在`pipeline`中添加`IdleStateHandler`

### 自定义Favicon
配置favicon的方式与spring-boot中完全一致。只需将`favicon.ico`文件放到classpath的根目录下即可。如下:
```
src/
  +- main/
    +- java/
    |   + <source code>
    +- resources/
        +- favicon.ico
```

### 自定义错误页面
配置自定义错误页面的方式与spring-boot中完全一致。你可以添加一个 `/public/error` 目录，错误页面将会是该目录下的静态页面，错误页面的文件名必须是准确的错误状态或者是一串掩码,如下：
```
src/
  +- main/
    +- java/
    |   + <source code>
    +- resources/
        +- public/
            +- error/
            |   +- 404.html
            |   +- 5xx.html
            +- <other public assets>
```  

### 多端点服务
- 在[快速启动](#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)的基础上，在多个需要成为端点的类上使用`@ServerEndpoint`、`@Component`注解即可
- 可通过`ServerEndpointExporter.getInetSocketAddressSet()`获取所有端点的地址
- 当地址不同时(即host不同或port不同)，使用不同的`ServerBootstrap`实例
- 当地址相同,路径(path)不同时,使用同一个`ServerBootstrap`实例
- 当多个端点服务的port为0时，将使用同一个随机的端口号
- 当多个端点的port和path相同时，host不能设为`"0.0.0.0"`，因为`"0.0.0.0"`意味着绑定所有的host



