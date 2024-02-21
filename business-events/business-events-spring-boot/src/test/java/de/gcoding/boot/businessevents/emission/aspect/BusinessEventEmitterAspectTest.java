package de.gcoding.boot.businessevents.emission.aspect;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessEventEmitterAspectTest {
    static final int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE;
    @Mock
    ProceedingJoinPoint joinPoint;
    @Mock
    MethodSignature methodSignature;
    @Mock
    BusinessEventsFactory businessEventsFactory;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Captor
    ArgumentCaptor<BusinessEvent> publishedEventsCaptor;
    @Mock
    EmitBusinessEvent configuration;
    BusinessEventEmitterAspect businessEventEmitterAspect;

    @BeforeEach
    void beforeEach() {
        setupTestObject(DEFAULT_ORDER);
    }

    @Test
    void whenMethodExecutesSuccessfullyAnEventIsEmitted() throws Throwable {
        whenMethodExecutedSuccessfullyAndReturns("payload");
        whenASingleEventIsGeneratedWithTheReturnValueAsPayload();

        businessEventEmitterAspect.emitEvents(joinPoint, this, configuration);

        assertThatOneEventIsEmittedWithPayload("payload");
    }

    @Test
    void whenMultipleEventsAreGeneratedForReturnValueAllAreEmitted() throws Throwable {
        whenMethodExecutedSuccessfullyAndReturns("irrelevant-payload-for-test");
        whenMultipleEventsAreGeneratedWithPayloads("first", "second", "third");

        businessEventEmitterAspect.emitEvents(joinPoint, this, configuration);

        assertThatMultipleEventsAreEmittedWithPayloads("first", "second", "third");
    }

    @Test
    void whenJoinPointIsNotAMethodExecutionAnExceptionShouldBeThrown() {
        when(joinPoint.getSignature()).thenReturn(mock(FieldSignature.class));

        final var error = assertThrows(
            BusinessEventAspectUsageException.class,
            () -> businessEventEmitterAspect.emitEvents(joinPoint, this, configuration)
        );

        assertThat(error).hasMessageContaining("@EmitBusinessEvent annotation can only be used on methods, but was used on signature");
    }

    @ParameterizedTest
    @ValueSource(classes = {Void.class, void.class})
    void whenMethodSignatureHasVoidReturnTypeAnExceptionIsThrown(Class<?> mockedReturnType) {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getReturnType()).thenReturn(mockedReturnType);

        final var error = assertThrows(
            BusinessEventAspectUsageException.class,
            () -> businessEventEmitterAspect.emitEvents(joinPoint, this, configuration)
        );

        assertThat(error).hasMessageContaining("@EmitBusinessEvent annotation can only be used on methods that return a value");
    }

    @Test
    void whenNoOrderIsSpecifiedThroughPropertiesLowestPriorityIsReturned() {
        final var order = businessEventEmitterAspect.getOrder();

        assertThat(order).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, 0, 10})
    void whenOrderIsSpecifiedItIsReturnedAccordingly(int desiredOrder) {
        setupTestObject(desiredOrder);

        final var order = businessEventEmitterAspect.getOrder();

        assertThat(order).isEqualTo(desiredOrder);
    }

    private void whenMethodExecutedSuccessfullyAndReturns(Object payload) {
        try {
            when(joinPoint.getSignature()).thenReturn(methodSignature);
            when(methodSignature.getReturnType()).thenReturn(payload.getClass());
            when(joinPoint.proceed()).thenReturn(payload);
        } catch (Throwable e) {
            throw new IllegalStateException("Unexpected error while setting up aspect test case", e);
        }
    }

    private void whenASingleEventIsGeneratedWithTheReturnValueAsPayload() {
        when(businessEventsFactory.createBusinessEvents(any(), eq(this), eq(methodSignature), eq(configuration)))
            .then(i -> List.of(BusinessEvent.withPayload(i.getArgument(0)).build()));
    }

    private void whenMultipleEventsAreGeneratedWithPayloads(Object... payloads) {
        when(businessEventsFactory.createBusinessEvents(any(), eq(this), eq(methodSignature), eq(configuration)))
            .then(i -> Arrays.stream(payloads).map(p -> BusinessEvent.withPayload(p).build()).toList());
    }

    private void assertThatOneEventIsEmittedWithPayload(Object payload) {
        assertThatMultipleEventsAreEmittedWithPayloads(payload);
    }

    private void assertThatMultipleEventsAreEmittedWithPayloads(Object... payloads) {
        verify(eventPublisher, times(payloads.length)).publishEvent(publishedEventsCaptor.capture());
        final var emittedEvents = publishedEventsCaptor.getAllValues();

        assertThat(emittedEvents)
            .hasSize(payloads.length)
            .allSatisfy(event -> assertThat(payloads).contains(event.getPayload()));
    }

    private void setupTestObject(int order) {
        businessEventEmitterAspect = new BusinessEventEmitterAspect(businessEventsFactory, eventPublisher, order);
    }
}