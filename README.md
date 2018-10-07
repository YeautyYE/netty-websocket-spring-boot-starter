netty-websocket-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[中文文档](https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README_zh.md) (Chinese Docs)

### About
netty-websocket-spring-boot-starter will help you develop WebSocket server by using Netty in spring-boot,it is easy to develop by using annotation like spring-websocket 

### Requirement
- jdk version 1.8 or 1.8+
- spring-boot version 2.0.0.RELEASE or over 2.0.0.RELEASE


### Quick Start

- add Dependencies:

```xml
	<dependency>
		<groupId>org.yeauty</groupId>
		<artifactId>netty-websocket-spring-boot-starter</artifactId>
		<version>0.6.4</version>
	</dependency>
```

- construct a `ServerEndpointExporter` instance to Spring container to start the WebSocket. e.g.

```java
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

- annotate `@ServerEndpoint`、`@Component` on endpoint class，and annotate `@OnOpen`、`@OnClose`、`@OnError`、`@OnMessage`、`@OnBinary`、`OnEvent` on the method. e.g.

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
        System.out.println(message);
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

- use Websocket client to connect `ws://127.0.0.1:80` 


### Annotation
###### @ServerEndpoint 
> declaring `ServerEndpointExporter` in Spring configuration,it will scan for WebSocket endpoints that 
> tated with `ServerEndpoint` .
> beans that be annotated with `ServerEndpoint` will be registered as a WebSocket endpoint.
> all [configurations](#configuration) are inside this annotation ( e.g. `@ServerEndpoint("/ws")` )

###### @OnOpen 
> when there is a WebSocket connection accepted,the method annotated with `@OnOpen` will be called  
> classes which be injected to the method are:Session、HttpHeaders

###### @OnClose
> when a WebSocket connection closed,the method annotated with `@OnClose` will be called
> classes which be injected to the method are:Session

###### @OnError
> when a WebSocket connection throw Throwable, the method annotated with `@OnError` will be called
> classes which be injected to the method are:Session、Throwable

###### @OnMessage
> when a WebSocket connection received a message,the method annotated with `@OnMessage` will be called
> classes which be injected to the method are:Session、String

###### @OnBinary
> when a WebSocket connection received the binary,the method annotated with `@OnBinary` will be called
> classes which be injected to the method are:Session、byte[]

###### @OnEvent
> when a WebSocket connection received the event of Netty,the method annotated with `@OnEvent` will be called
> classes which be injected to the method are:Session、Object

### Configuration
> all configurations are configured in `@ServerEndpoint`'s property 

| property  | default | description 
|---|---|---
|path|"/"|path of WebSocket can be aliased for `value`
|host|"0.0.0.0"|host of WebSocket.`"0.0.0.0"` means all of local addresses
|port|80|port of WebSocket。if the port equals to 0，it will use a random and available port(to get the port [Multi-Endpoint](#multi-endpoint))
|bossLoopGroupThreads|0|num of threads in bossEventLoopGroup
|workerLoopGroupThreads|0|num of threads in workerEventLoopGroup
|useCompressionHandler|false|whether add WebSocketServerCompressionHandler to pipeline
|prefix|""|configuration by application.properties when it is empty，details can be find in [Configuration-by-application.properties](#configuration-by-application.properties)
|optionConnectTimeoutMillis|30000|the same as `ChannelOption.CONNECT_TIMEOUT_MILLIS` in Netty
|optionSoBacklog|128|the same as `ChannelOption.SO_BACKLOG` in Netty
|childOptionWriteSpinCount|16|the same as `ChannelOption.WRITE_SPIN_COUNT` in Netty
|childOptionWriteBufferHighWaterMark|64*1024|the same as `ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK` in Netty,but use `ChannelOption.WRITE_BUFFER_WATER_MARK` in fact.
|childOptionWriteBufferLowWaterMark|32*1024|the same as `ChannelOption.WRITE_BUFFER_LOW_WATER_MARK` in Netty,but use `ChannelOption.WRITE_BUFFER_WATER_MARK` in fact.
|childOptionSoRcvbuf|-1(mean not set)|the same as `ChannelOption.SO_RCVBUF` in Netty
|childOptionSoSndbuf|-1(mean not set)|the same as `ChannelOption.SO_SNDBUF` in Netty
|childOptionTcpNodelay|true|the same as `ChannelOption.TCP_NODELAY` in Netty
|childOptionSoKeepalive|false|the same as `ChannelOption.SO_KEEPALIVE` in Netty
|childOptionSoLinger|-1|the same as `ChannelOption.SO_LINGER` in Netty
|childOptionAllowHalfClosure|false|the same as `ChannelOption.ALLOW_HALF_CLOSURE` in Netty
|readerIdleTimeSeconds|0|the same as `readerIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0
|writerIdleTimeSeconds|0|the same as `writerIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0
|allIdleTimeSeconds|0|the same as `allIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0

### Configuration by application.properties
> After setting `prefix` in `@ServerEndpoint`, then configurate by `application.properties`. for example：

- first,set `prefix` 
```java
@ServerEndpoint(prefix = "netty-websocket")
@Component
public class MyWebSocket {
    ...
}
```
- then configurate in `application.properties`
```
netty-websocket.host=0.0.0.0
netty-websocket.path=/
netty-websocket.port=80
```

> The mapping of the key of `application.properties` and properties in `@ServerEndpoint`,following:

| properties in `@ServerEndpoint` | key of `application.properties` | example
|---|---|---
|path|{prefix}.path|netty-websocket.path
|host|{prefix}.host|netty-websocket.host
|port|{prefix}.port|netty-websocket.port
|bossLoopGroupThreads|{prefix}.boss-loop-group-threads|netty-websocket.boss-loop-group-threads
|workerLoopGroupThreads|{prefix}.worker-loop-group-threads|netty-websocket.worker-loop-group-threads
|useCompressionHandler|{prefix}.use-compression-handler|netty-websocket.use-compression-handler
|optionConnectTimeoutMillis|{prefix}.option.connect-timeout-millis|netty-websocket.option.connect-timeout-millis
|optionSoBacklog|{prefix}.option.so-backlog|netty-websocket.option.so-backlog
|childOptionWriteSpinCount|{prefix}.child-option.write-spin-count|netty-websocket.child-option.write-spin-count
|childOptionWriteBufferHighWaterMark|{prefix}.child-option.write-buffer-high-water-mark|netty-websocket.child-option.write-buffer-high-water-mark
|childOptionWriteBufferLowWaterMark|{prefix}.child-option.write-buffer-low-water-mark|netty-websocket.child-option.write-buffer-low-water-mark
|childOptionSoRcvbuf|{prefix}.child-option.so-rcvbuf|netty-websocket.child-option.so-rcvbuf
|childOptionSoSndbuf|{prefix}.child-option.so-sndbuf|netty-websocket.child-option.so-sndbuf
|childOptionTcpNodelay|{prefix}.child-option.tcp-nodelay|netty-websocket.child-option.tcp-nodelay
|childOptionSoKeepalive|{prefix}.child-option.so-keepalive|netty-websocket.child-option.so-keepalive
|childOptionSoLinger|{prefix}.child-option.so-linger|netty-websocket.child-option.so-linger
|childOptionAllowHalfClosure|{prefix}.child-option.allow-half-closure|netty-websocket.child-option.allow-half-closure
|readerIdleTimeSeconds|{prefix}.reader-idle-time-seconds|netty-websocket.reader-idle-time-seconds
|writerIdleTimeSeconds|{prefix}.writer-idle-time-seconds|netty-websocket.writer-idle-time-seconds
|allIdleTimeSeconds|{prefix}.all-idle-time-seconds|netty-websocket.all-idle-time-seconds

### Custom Favicon
The way of configure favicon is the same as spring-boot.If `favicon.ico` is presented in the root of the classpath,it will be automatically used as the favicon of the application.the example is following:
```
src/
  +- main/
      +- java/
      |   + <source code>
      +- resources/
          +- favicon.ico
```

### Custom Error Pages
The way of configure favicon is the same as spring-boot.you can add a file to an `/public/error`
folder.The name of the error page should be the exact status code or a series mask.the example is following:
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

### Multi Endpoint
- base on [Quick-Start](#quick-start),use annotation `@ServerEndpoint` and `@Component` in classes which hope to become a endpoint.
- you can get all socket addresses in `ServerEndpointExporter.getInetSocketAddressSet()`.
- when there are different addresses(different host or different port) in WebSocket,they will use different `ServerBootstrap` instance.
- when the addresses are the same,but path is different,they will use the same `ServerBootstrap` instance.
- when multiple port of endpoint is 0 ,they will use the same random port
- when multiple port of endpoint is the same as the path,host can't be set as "0.0.0.0",because it means it binds all of the addresses


