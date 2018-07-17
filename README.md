netty-websocket-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[中文文档](https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README_zh.md)

### About
netty-websocket-spring-boot-starter will help you develop WebSocket server by using Netty in spring-boot,it is easy to develop by using annotation like spring-websocket 

### Requirement
- jdk version 1.8 or 1.8+


### Quick Start
- clone source and install to Maven repository

```
mvn clean install
```

- add Dependencies:

```xml
	<dependency>
		<groupId>com.yeauty</groupId>
		<artifactId>netty-websocket-spring-boot-starter</artifactId>
		<version>0.1.6-SNAPSHOT</version>
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

- annotate `@ServerEndpoint`、`@Component` on endpoint class，and annotate `@OnOpen`、`@OnClose`、`@OnError`、`@OnMessage`、`@OnBinary` on the method. e.g.

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
}
```

- use Websocket client to connect `ws://127.0.0.1:80` 


### Annotation
###### @ServerEndpoint 
> declaring `ServerEndpointExporter` in Spring configuration,it will scan for WebSocket endpoints that annotated with `ServerEndpoint` .
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

### Configuration
> all configurations are configured in `@ServerEndpoint`'s property 

| property  | default | description 
|---|---|---
|path|"/"|path of WebSocket can be aliased for `value`
|host|"0.0.0.0"|host of WebSocket.`"0.0.0.0"` means all of local addresses
|port|80|port of WebSocket。if the port equals to 0，it will use a random and available port(to get the port [Multi-Endpoint](#multi-endpoint))
|tcpNodelay|true|the same as `ChannelOption.TCP_NODELAY` in Netty
|backlog|1024|the same as `ChannelOption.SO_BACKLOG` in Netty


### Multi Endpoint
- base on [Quick-Start](#quick-start),use annotation `@ServerEndpoint` and `@Component` in classes which hope to become a endpoint.
- you can get all socket addresses in `ServerEndpointExporter.getInetSocketAddressSet()`.
- when there are different addresses(different host or different port) in WebSocket,they will use different `ServerBootstrap` instance.
- when the addresses are the same,but path is different,they will use the same `ServerBootstrap` instance.
- when multiple port of endpoint is 0 ,they will use the same random port
- when multiple port of endpoint is the same as the path,host can't be set as "0.0.0.0",because it means it binds all of the addresses


