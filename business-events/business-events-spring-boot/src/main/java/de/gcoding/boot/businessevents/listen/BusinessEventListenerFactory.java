package de.gcoding.boot.businessevents.listen;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class BusinessEventListenerFactory implements EventListenerFactory, Ordered {
    private final BeanFactory beanFactory;

    public BusinessEventListenerFactory(@NonNull BeanFactory beanFactory) {
        this.beanFactory = requireNonNull(beanFactory);
    }

    @Override
    public boolean supportsMethod(@NonNull Method method) {
        return AnnotationUtils.findAnnotation(method, BusinessEventListener.class) != null;
    }

    @Override
    @NonNull
    public ApplicationListener<?> createApplicationListener(@NonNull String beanName, @NonNull Class<?> type, @NonNull Method method) {
        final var configuration = AnnotationUtils.findAnnotation(method, BusinessEventListener.class);
        requireNonNull(configuration, "Illegal usage of createApplicationListener, should only be invoked if supportsMethod returns true");

        return new BusinessEventListenerMethodAdapter(
            configuration,
            () -> beanFactory.getBean(beanName),
            method
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
