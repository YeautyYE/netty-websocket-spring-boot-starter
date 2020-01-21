package org.yeauty.support;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.springframework.core.MethodParameter;
import org.yeauty.annotation.OnBinary;

public class ByteMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnBinary.class) && byte[].class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) object;
        ByteBuf content = binaryWebSocketFrame.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }
}
