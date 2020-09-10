netty-websocket-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[English Docs](https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README.md)

### 简介
本项目帮助你在spring-boot中使用Netty来开发WebSocket服务器，并像spring-websocket的注解开发一样简单

### 要求
- jdk版本为1.8或1.8+


### 快速开始

- 添加依赖:

```xml
	<dependency>
		<groupId>org.yeauty</groupId>
		<artifactId>netty-websocket-spring-boot-starter</artifactId>
		<version>0.9.5</version>
	</dependency>
```

- 在端点类上加上`@ServerEndpoint`注解，并在相应的方法上加上`@BeforeHandshake`、`@OnOpen`、`@OnClose`、`@OnError`、`@OnMessage`、`@OnBinary`、`@OnEvent`注解，样例如下：

```java
@ServerEndpoint(path = "/ws/{arg}")
public class MyWebSocket {

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
        session.setSubprotocols("stomp");
        if (!"ok".equals(req)){
            System.out.println("Authentication failed!");
            session.close();
        }
    }
    
    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
        System.out.println("new connection");
        System.out.println(req);
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
    public void onMessage(Session session, String message) {
        System.out.println(message);
        session.sendText("Hello Netty!");
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
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
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }

}
```

- 打开WebSocket客户端，连接到`ws://127.0.0.1:80/ws/xxx`


### 注解
###### @ServerEndpoint 
> 当ServerEndpointExporter类通过Spring配置进行声明并被使用，它将会去扫描带有@ServerEndpoint注解的类
> 被注解的类将被注册成为一个WebSocket端点
> 所有的[配置项](#%E9%85%8D%E7%BD%AE)都在这个注解的属性中 ( 如:`@ServerEndpoint("/ws")` )

###### @BeforeHandshake 
> 当有新的连接进入时，对该方法进行回调
> 注入参数的类型:Session、HttpHeaders...

###### @OnOpen 
> 当有新的WebSocket连接完成时，对该方法进行回调
> 注入参数的类型:Session、HttpHeaders...

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

###### @OnEvent
> 当接收到Netty的事件时，对该方法进行回调
> 注入参数的类型:Session、Object

### 配置
> 所有的配置项都在这个注解的属性中

| 属性  | 默认值 | 说明 
|---|---|---
|path|"/"|WebSocket的path,也可以用`value`来设置
|host|"0.0.0.0"|WebSocket的host,`"0.0.0.0"`即是所有本地地址
|port|80|WebSocket绑定端口号。如果为0，则使用随机端口(端口获取可见 [多端点服务](#%E5%A4%9A%E7%AB%AF%E7%82%B9%E6%9C%8D%E5%8A%A1))
|bossLoopGroupThreads|0|bossEventLoopGroup的线程数
|workerLoopGroupThreads|0|workerEventLoopGroup的线程数
|useCompressionHandler|false|是否添加WebSocketServerCompressionHandler到pipeline
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
|maxFramePayloadLength|65536|最大允许帧载荷长度

### 通过application.properties进行配置
> 所有参数皆可使用`${...}`占位符获取`application.properties`中的配置。如下：

- 首先在`@ServerEndpoint`注解的属性中使用`${...}`占位符
```java
@ServerEndpoint(host = "${ws.host}",port = "${ws.port}")
public class MyWebSocket {
    ...
}
```
- 接下来即可在`application.properties`中配置
```
ws.host=0.0.0.0
ws.port=80
```

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

---
### 更新日志

#### 0.8.0

- 自动装配

#### 0.9.0

- 通过`@PathVariable`支持RESTful风格中获取参数
- 通过`@RequestParam`实现请求中query的获取参数
- 移除原来的ParameterMap,用`@RequestParam MultiValueMap`代替
- 新增 `@BeforeHandshake` 注解，可在握手之前对连接进行关闭
- 在`@BeforeHandshake`事件中可设置子协议
- 去掉配置端点类上的 `@Component`
- 更新`Netty`版本到 `4.1.44.Final`

#### 0.9.1

- 修复bug：当使用`@RequestParam MultiValueMap`时获取的对象为null
- 更新`Netty`版本到 `4.1.45.Final`

#### 0.9.2

- 兼容 0.8.0 以下版本，可以手动装配`ServerEndpointExporter`对象

#### 0.9.3

- 修复bug：当没有 `@BeforeHandshake`时会出现空指针异常

#### 0.9.4

- 修复bug：当没有 `@BeforeHandshake`时 `OnOpen`中的`Session`为null.

#### 0.9.5

- 修复bug：`OnError`事件中的`Throwable`为null.