package org.yeauty.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


public class DefaultPathMatcher implements WsPathMatcher {

    private String pattern;

    public DefaultPathMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        if (!pattern.equals(decoder.path())) {
            return false;
        }
        return true;
    }
}
