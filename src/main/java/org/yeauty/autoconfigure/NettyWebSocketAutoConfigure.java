package org.yeauty.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.yeauty.annotation.EnableWebSocket;
import org.yeauty.standard.ServerEndpointExporter;

@EnableWebSocket
@ConditionalOnMissingBean(ServerEndpointExporter.class)
public class NettyWebSocketAutoConfigure {
}
