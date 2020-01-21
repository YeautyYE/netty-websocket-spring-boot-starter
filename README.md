netty-websocket-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[中文文档](https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README_zh.md) (Chinese Docs)

### About
netty-websocket-spring-boot-starter will help you develop WebSocket server by using Netty in spring-boot,it is easy to develop by using annotation like spring-websocket 

### Requirement
- jdk version 1.8 or 1.8+


### Quick Start

- add Dependencies:

```xml
	<dependency>
		<groupId>org.yeauty</groupId>
		<artifactId>netty-websocket-spring-boot-starter</artifactId>
		<version>0.9.0</version>
	</dependency>
```

- annotate `@ServerEndpoint` on endpoint class，and annotate `@BeforeHandshake`,`@OnOpen`,`@OnClose`,`@OnError`,`@OnMessage`,`@OnBinary`,`@OnEvent` on the method. e.g.

```java
@ServerEndpoint(path = "/ws/{arg}")
public class MyWebSocket {

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
        session.setSubprotocols("stomp");
        if (!req.equals("ok")){
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

- use Websocket client to connect `ws://127.0.0.1:80/ws/xxx` 


### Annotation
###### @ServerEndpoint 
> declaring `ServerEndpointExporter` in Spring configuration,it will scan for WebSocket endpoints that be annotated  with `ServerEndpoint` .
> beans that be annotated with `ServerEndpoint` will be registered as a WebSocket endpoint.
> all [configurations](#configuration) are inside this annotation ( e.g. `@ServerEndpoint("/ws")` )

###### @BeforeHandshake 
> when there is a connection accepted,the method annotated with `@BeforeHandshake` will be called  
> classes which be injected to the method are:Session,HttpHeaders...

###### @OnOpen 
> when there is a WebSocket connection completed,the method annotated with `@OnOpen` will be called  
> classes which be injected to the method are:Session,HttpHeaders...

###### @OnClose
> when a WebSocket connection closed,the method annotated with `@OnClose` will be called
> classes which be injected to the method are:Session

###### @OnError
> when a WebSocket connection throw Throwable, the method annotated with `@OnError` will be called
> classes which be injected to the method are:Session,Throwable

###### @OnMessage
> when a WebSocket connection received a message,the method annotated with `@OnMessage` will be called
> classes which be injected to the method are:Session,String

###### @OnBinary
> when a WebSocket connection received the binary,the method annotated with `@OnBinary` will be called
> classes which be injected to the method are:Session,byte[]

###### @OnEvent
> when a WebSocket connection received the event of Netty,the method annotated with `@OnEvent` will be called
> classes which be injected to the method are:Session,Object

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
|maxFramePayloadLength|65536|Maximum allowable frame payload length.

### Configuration by application.properties
> You can get the configurate of `application.properties` by using `${...}` placeholders. for example：

- first,use `${...}` in `@ServerEndpoint` 
```java
@ServerEndpoint(host = "${ws.host}",port = "${ws.port})
public class MyWebSocket {
    ...
}
```
- then configurate in `application.properties`
```
ws.host=0.0.0.0
ws.port=80
```

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

---
### Change Log

#### 0.8.0

- Auto-Configuration

#### 0.9.0

- Support RESTful by `@PathVariable`
- Get param by`@RequestParam` from query
- Remove `ParameterMap` ,instead of `@RequestParam MultiValueMap`
- Add `@BeforeHandshake` annotation，you can close the connect before handshake
- Set sub-protocol in `@BeforeHandshake` event
- Remove  the `@Component` on endpoint class
- Update `Netty` version to `4.1.44.Final`
