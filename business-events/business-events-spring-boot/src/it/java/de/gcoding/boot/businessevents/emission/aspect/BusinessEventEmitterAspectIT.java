package de.gcoding.boot.businessevents.emission.aspect;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


public class BusinessEventEmitterAspectIT {
    static final String PAYLOAD = "payload data";
    static final List<BusinessEvent> EMITTED_EVENTS = Collections.synchronizedList(new LinkedList<>());

    @SpringBootTest(classes = {EmitBusinessEventTestConfiguration.class})
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    public static abstract class BaseEmitBusinessEventTest {
        @SpyBean
        ServiceWithAnnotatedMethod eventEmittingService;
        @Autowired
        ConfigurableApplicationContext applicationContext;

        protected abstract void assertThatAspectIsExecutedInExpectedOrder();

        @AfterEach
        void afterEach() {
            EMITTED_EVENTS.clear();
        }

        @Test
        void whenAnnotatedMethodIsInvokedAnEventIsEmitted() {
            final var expectedPayload = eventEmittingService.annotatedMethod();

            assertThat(EMITTED_EVENTS)
                .hasSize(1)
                .allSatisfy(event -> assertThat(event.getPayload()).isEqualTo(expectedPayload).isEqualTo(PAYLOAD));

            verify(eventEmittingService).annotatedMethod();
        }

        @Test
        void whenAspectIsInvokedItIsOrderedAccordingToItsOrderConfiguration() throws Exception {
            try (final var ignored = interceptABusinessEventListenerInvocation(this::assertThatAspectIsExecutedInExpectedOrder)) {
                eventEmittingService.annotatedMethod();
            }
        }

        private AutoCloseable interceptABusinessEventListenerInvocation(Runnable runnable) {
            final ApplicationListener<BusinessEvent> listener = event -> {
                runnable.run();
            };
            applicationContext.addApplicationListener(listener);

            return () -> applicationContext.removeApplicationListener(listener);
        }
    }

    @Nested
    @TestPropertySource(properties = {"order=-1"})
    public class TestWithHigherOrder extends BaseEmitBusinessEventTest {
        @Override
        protected void assertThatAspectIsExecutedInExpectedOrder() {
            assertThat(MockOrderedAspect.isExecutedWithin()).isFalse();
        }
    }

    @Nested
    @TestPropertySource(properties = {"order=1"})
    public class TestWithLowerOrder extends BaseEmitBusinessEventTest {
        @Override
        protected void assertThatAspectIsExecutedInExpectedOrder() {
            assertThat(MockOrderedAspect.isExecutedWithin()).isTrue();
        }
    }

    public static class ServiceWithAnnotatedMethod {
        @MockAspect
        @EmitBusinessEvent
        public String annotatedMethod() {
            return PAYLOAD;
        }
    }

    @Configuration
    public static class EmitBusinessEventTestConfiguration {
        @Value("${order}")
        private int order;

        @Bean
        public BusinessEventEmitterAspect businessEventEmitterAspect(
            BusinessEventsFactory businessEventsFactory,
            ApplicationEventPublisher eventPublisher
        ) {
            return new BusinessEventEmitterAspect(businessEventsFactory, eventPublisher, order);
        }

        @Bean
        public BusinessEventsFactory businessEventsFactory() {
            return new MockBusinessEventsFactory();
        }

        @Bean
        public ServiceWithAnnotatedMethod eventEmittingService() {
            return new ServiceWithAnnotatedMethod();
        }

        @Bean
        public ApplicationListener<BusinessEvent> businessEventApplicationListener() {
            return EMITTED_EVENTS::add;
        }

        @Bean
        public MockOrderedAspect mockOrderedAspect() {
            return new MockOrderedAspect();
        }
    }

    public static class MockBusinessEventsFactory implements BusinessEventsFactory {
        @Nonnull
        @Override
        public List<BusinessEvent> createBusinessEvents(@Nullable Object payload, @Nonnull Object emittingSource, @Nonnull MethodSignature methodSignature, @Nonnull EmitBusinessEvent configuration) {
            return List.of(BusinessEvent.withPayload(requireNonNull(payload)).build());
        }
    }

    @Aspect
    @Order(0)
    public static class MockOrderedAspect {
        private static final ThreadLocal<Boolean> CONTEXT = new ThreadLocal<>();

        public static boolean isExecutedWithin() {
            return Boolean.TRUE.equals(CONTEXT.get());
        }

        @SuppressWarnings("unused")
        @Around("@annotation(mockAspect)")
        public Object mockAspectInvocation(ProceedingJoinPoint joinPoint, MockAspect mockAspect) throws Throwable {
            CONTEXT.set(true);
            try {
                return joinPoint.proceed();
            } finally {
                CONTEXT.remove();
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MockAspect {
    }
}
