package org.yeauty.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;
import org.yeauty.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import static org.yeauty.pojo.PojoEndpointServer.REQUEST_PARAM;

public class RequestParamMethodArgumentResolver implements MethodArgumentResolver {

    private AbstractBeanFactory beanFactory;

    public RequestParamMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        String name = ann.name();
        if (name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + parameter.getNestedParameterType().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }

        if (!channel.hasAttr(REQUEST_PARAM)) {
            QueryStringDecoder decoder = new QueryStringDecoder(((FullHttpRequest) object).uri());
            channel.attr(REQUEST_PARAM).set(decoder.parameters());
        }

        Map<String, List<String>> requestParams = channel.attr(REQUEST_PARAM).get();
        List<String> arg = (requestParams != null ? requestParams.get(name) : null);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        if (arg == null) {
            if ("\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n".equals(ann.defaultValue())) {
                return null;
            }else {
                return typeConverter.convertIfNecessary(ann.defaultValue(), parameter.getParameterType());
            }
        }
        if (List.class.isAssignableFrom(parameter.getParameterType())) {
            return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
        } else {
            return typeConverter.convertIfNecessary(arg.get(0), parameter.getParameterType());
        }
    }
}
