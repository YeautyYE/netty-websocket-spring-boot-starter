package org.yeauty.pojo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.yeauty.annotation.*;
import org.yeauty.exception.DeploymentException;
import org.yeauty.support.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PojoMethodMapping {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Method beforeHandshake;
    private final Method onOpen;
    private final Method onClose;
    private final Method onError;
    private final Method onMessage;
    private final Method onBinary;
    private final Method onEvent;
    private final MethodParameter[] beforeHandshakeParameters;
    private final MethodParameter[] onOpenParameters;
    private final MethodParameter[] onCloseParameters;
    private final MethodParameter[] onErrorParameters;
    private final MethodParameter[] onMessageParameters;
    private final MethodParameter[] onBinaryParameters;
    private final MethodParameter[] onEventParameters;
    private final MethodArgumentResolver[] beforeHandshakeArgResolvers;
    private final MethodArgumentResolver[] onOpenArgResolvers;
    private final MethodArgumentResolver[] onCloseArgResolvers;
    private final MethodArgumentResolver[] onErrorArgResolvers;
    private final MethodArgumentResolver[] onMessageArgResolvers;
    private final MethodArgumentResolver[] onBinaryArgResolvers;
    private final MethodArgumentResolver[] onEventArgResolvers;
    private final Class pojoClazz;
    private final ApplicationContext applicationContext;
    private final AbstractBeanFactory beanFactory;

    public PojoMethodMapping(Class<?> pojoClazz, ApplicationContext context, AbstractBeanFactory beanFactory) throws DeploymentException {
        this.applicationContext = context;
        this.pojoClazz = pojoClazz;
        this.beanFactory = beanFactory;
        Method handshake = null;
        Method open = null;
        Method close = null;
        Method error = null;
        Method message = null;
        Method binary = null;
        Method event = null;
        Method[] pojoClazzMethods = null;
        Class<?> currentClazz = pojoClazz;
        while (!currentClazz.equals(Object.class)) {
            Method[] currentClazzMethods = currentClazz.getDeclaredMethods();
            if (currentClazz == pojoClazz) {
                pojoClazzMethods = currentClazzMethods;
            }
            for (Method method : currentClazzMethods) {
                if (method.getAnnotation(BeforeHandshake.class) != null) {
                    checkPublic(method);
                    if (handshake == null) {
                        handshake = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(handshake, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation BeforeHandshake");
                        }
                    }
                } else if (method.getAnnotation(OnOpen.class) != null) {
                    checkPublic(method);
                    if (open == null) {
                        open = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(open, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnOpen");
                        }
                    }
                } else if (method.getAnnotation(OnClose.class) != null) {
                    checkPublic(method);
                    if (close == null) {
                        close = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(close, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnClose");
                        }
                    }
                } else if (method.getAnnotation(OnError.class) != null) {
                    checkPublic(method);
                    if (error == null) {
                        error = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(error, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnError");
                        }
                    }
                } else if (method.getAnnotation(OnMessage.class) != null) {
                    checkPublic(method);
                    if (message == null) {
                        message = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(message, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation onMessage");
                        }
                    }
                } else if (method.getAnnotation(OnBinary.class) != null) {
                    checkPublic(method);
                    if (binary == null) {
                        binary = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(binary, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnBinary");
                        }
                    }
                } else if (method.getAnnotation(OnEvent.class) != null) {
                    checkPublic(method);
                    if (event == null) {
                        event = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(event, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnEvent");
                        }
                    }
                } else {
                    // Method not annotated
                }
            }
            currentClazz = currentClazz.getSuperclass();
        }
        // If the methods are not on pojoClazz and they are overridden
        // by a non annotated method in pojoClazz, they should be ignored
        if (handshake != null && handshake.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, handshake, BeforeHandshake.class)) {
                handshake = null;
            }
        }
        if (open != null && open.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, open, OnOpen.class)) {
                open = null;
            }
        }
        if (close != null && close.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, close, OnClose.class)) {
                close = null;
            }
        }
        if (error != null && error.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, error, OnError.class)) {
                error = null;
            }
        }
        if (message != null && message.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, message, OnMessage.class)) {
                message = null;
            }
        }
        if (binary != null && binary.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, binary, OnBinary.class)) {
                binary = null;
            }
        }
        if (event != null && event.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, event, OnEvent.class)) {
                event = null;
            }
        }

        this.beforeHandshake = handshake;
        this.onOpen = open;
        this.onClose = close;
        this.onError = error;
        this.onMessage = message;
        this.onBinary = binary;
        this.onEvent = event;
        beforeHandshakeParameters = getParameters(beforeHandshake);
        onOpenParameters = getParameters(onOpen);
        onCloseParameters = getParameters(onClose);
        onMessageParameters = getParameters(onMessage);
        onErrorParameters = getParameters(onError);
        onBinaryParameters = getParameters(onBinary);
        onEventParameters = getParameters(onEvent);
        beforeHandshakeArgResolvers = getResolvers(beforeHandshakeParameters);
        onOpenArgResolvers = getResolvers(onOpenParameters);
        onCloseArgResolvers = getResolvers(onCloseParameters);
        onMessageArgResolvers = getResolvers(onMessageParameters);
        onErrorArgResolvers = getResolvers(onErrorParameters);
        onBinaryArgResolvers = getResolvers(onBinaryParameters);
        onEventArgResolvers = getResolvers(onEventParameters);
    }

    private void checkPublic(Method m) throws DeploymentException {
        if (!Modifier.isPublic(m.getModifiers())) {
            throw new DeploymentException(
                    "pojoMethodMapping.methodNotPublic " + m.getName());
        }
    }

    private boolean isMethodOverride(Method method1, Method method2) {
        return (method1.getName().equals(method2.getName())
                && method1.getReturnType().equals(method2.getReturnType())
                && Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes()));
    }

    private boolean isOverridenWithoutAnnotation(Method[] methods, Method superclazzMethod, Class<? extends Annotation> annotation) {
        for (Method method : methods) {
            if (isMethodOverride(method, superclazzMethod)
                    && (method.getAnnotation(annotation) == null)) {
                return true;
            }
        }
        return false;
    }

    Object getEndpointInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object implement = pojoClazz.getDeclaredConstructor().newInstance();
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(implement);
        return implement;
    }

    Method getBeforeHandshake() {
        return beforeHandshake;
    }

    Object[] getBeforeHandshakeArgs(Channel channel, FullHttpRequest req) throws Exception {
        return getMethodArgumentValues(channel, req, beforeHandshakeParameters, beforeHandshakeArgResolvers);
    }

    Method getOnOpen() {
        return onOpen;
    }

    Object[] getOnOpenArgs(Channel channel, FullHttpRequest req) throws Exception {
        return getMethodArgumentValues(channel, req, onOpenParameters, onOpenArgResolvers);
    }

    MethodArgumentResolver[] getOnOpenArgResolvers() {
        return onOpenArgResolvers;
    }

    Method getOnClose() {
        return onClose;
    }

    Object[] getOnCloseArgs(Channel channel) throws Exception {
        return getMethodArgumentValues(channel, null, onCloseParameters, onCloseArgResolvers);
    }

    Method getOnError() {
        return onError;
    }

    Object[] getOnErrorArgs(Channel channel, Throwable throwable) throws Exception {
        return getMethodArgumentValues(channel, throwable, onErrorParameters, onErrorArgResolvers);
    }

    Method getOnMessage() {
        return onMessage;
    }

    Object[] getOnMessageArgs(Channel channel, TextWebSocketFrame textWebSocketFrame) throws Exception {
        return getMethodArgumentValues(channel, textWebSocketFrame, onMessageParameters, onMessageArgResolvers);
    }

    Method getOnBinary() {
        return onBinary;
    }

    Object[] getOnBinaryArgs(Channel channel, BinaryWebSocketFrame binaryWebSocketFrame) throws Exception {
        return getMethodArgumentValues(channel, binaryWebSocketFrame, onBinaryParameters, onBinaryArgResolvers);
    }

    Method getOnEvent() {
        return onEvent;
    }

    Object[] getOnEventArgs(Channel channel, Object evt) throws Exception {
        return getMethodArgumentValues(channel, evt, onEventParameters, onEventArgResolvers);
    }

    private Object[] getMethodArgumentValues(Channel channel, Object object, MethodParameter[] parameters, MethodArgumentResolver[] resolvers) throws Exception {
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            MethodArgumentResolver resolver = resolvers[i];
            Object arg = resolver.resolveArgument(parameter, channel, object);
            objects[i] = arg;
        }
        return objects;
    }

    private MethodArgumentResolver[] getResolvers(MethodParameter[] parameters) throws DeploymentException {
        MethodArgumentResolver[] methodArgumentResolvers = new MethodArgumentResolver[parameters.length];
        List<MethodArgumentResolver> resolvers = getDefaultResolvers();
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            for (MethodArgumentResolver resolver : resolvers) {
                if (resolver.supportsParameter(parameter)) {
                    methodArgumentResolvers[i] = resolver;
                    break;
                }
            }
            if (methodArgumentResolvers[i] == null) {
                throw new DeploymentException("pojoMethodMapping.paramClassIncorrect parameter name : " + parameter.getParameterName());
            }
        }
        return methodArgumentResolvers;
    }

    private List<MethodArgumentResolver> getDefaultResolvers() {
        List<MethodArgumentResolver> resolvers = new ArrayList<>();
        resolvers.add(new SessionMethodArgumentResolver());
        resolvers.add(new HttpHeadersMethodArgumentResolver());
        resolvers.add(new TextMethodArgumentResolver());
        resolvers.add(new ThrowableMethodArgumentResolver());
        resolvers.add(new ByteMethodArgumentResolver());
        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new RequestParamMethodArgumentResolver(beanFactory));
        resolvers.add(new PathVariableMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver(beanFactory));
        resolvers.add(new EventMethodArgumentResolver(beanFactory));
        return resolvers;
    }

    private static MethodParameter[] getParameters(Method m) {
        if (m == null) {
            return new MethodParameter[0];
        }
        int count = m.getParameterCount();
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            MethodParameter methodParameter = new MethodParameter(m, i);
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            result[i] = methodParameter;
        }
        return result;
    }
}