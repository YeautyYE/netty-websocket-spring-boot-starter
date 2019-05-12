package org.yeauty.annotation;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.yeauty.standard.ServerEndpointExporter;

public class NettyWebSocketSelector implements ImportSelector, Ordered {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return of(ServerEndpointExporter.class.getName());
    }

    private static <T> T[] of(T... values) {
        return values;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
