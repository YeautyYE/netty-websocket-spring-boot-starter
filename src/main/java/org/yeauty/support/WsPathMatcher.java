package org.yeauty.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


public interface WsPathMatcher {

    String getPattern();

    boolean matchAndExtract(QueryStringDecoder decoder, Channel channel);
}
