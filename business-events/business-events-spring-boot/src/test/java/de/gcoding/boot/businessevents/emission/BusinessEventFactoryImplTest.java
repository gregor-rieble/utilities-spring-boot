package de.gcoding.boot.businessevents.emission;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.EventActions;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessEventFactoryImplTest {
    BusinessEventFactoryImpl businessEventFactory;
    @Mock
    MethodSignature methodSignature;
    @Mock
    EmitBusinessEvent configuration;

    @BeforeEach
    void beforeEach() {
        final var beanFactory = new StaticListableBeanFactory(Map.of("mockBean", new MockBean()));

        businessEventFactory = new BusinessEventFactoryImpl(
            new SpelExpressionParser(),
            new BeanFactoryResolver(beanFactory)
        );
    }

    @Test
    void whenBusinessEventIsCreatedItIsAnInstanceOfSpringBusinessEvent() {
        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event).isInstanceOf(BusinessEvent.class);
    }

    @Test
    void whenSourceIsPassedItIsUsedInEvent() {
        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getSource()).isEqualTo(this);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third"})
    void whenPayloadIsPassedItIsUsedInEvent(String payload) {
        final var event = businessEventFactory.createBusinessEvent(payload, this, methodSignature, configuration);
        assertThat(event.getPayload()).isEqualTo(payload);
    }

    @Test
    void whenNoActionIsSetInConfigurationTheDefaultActionIsNone() {
        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo(EventActions.NONE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"custom", "action"})
    void whenActionIsSetInConfigurationCreatedEventWillHaveTheSameAction(String action) {
        when(configuration.action()).thenReturn(action);

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo(action);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void whenAnEmptyActionIsSetInConfigurationNoneActionWillBeUsed(String emptyAction) {
        when(configuration.action()).thenReturn(emptyAction);

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo(EventActions.NONE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"custom", "dynamic", "other"})
    void whenActionSpELIsSetActionWillBeEvaluatedDynamically(String action) {
        when(configuration.actionSpEL()).thenReturn("payload.action");

        final var payload = new PayloadWithDynamicAction(action);
        final var event = businessEventFactory.createBusinessEvent(payload, this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo(action);
    }

    @Test
    void whenActionSpELEvaluatesToEmptyStringStaticActionIsUsedAsFallback() {
        when(configuration.actionSpEL()).thenReturn("'  '");
        when(configuration.action()).thenReturn(EventActions.CREATE);

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo(EventActions.CREATE);
    }

    @Test
    void whenActionSpELIsEvaluatedPayloadIsPresentInRoot() {
        when(configuration.actionSpEL()).thenReturn("payload == 'payload'");

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    @Test
    void whenActionSpELIsEvaluatedEmittingSourceIsPresentInRoot() {
        when(configuration.actionSpEL()).thenReturn("emittingSource.calledBySpELInTest == 'was called'");

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    @Test
    void whenActionSpELIsEvaluatedMethodSignatureIsPresentInRoot() {
        when(methodSignature.getName()).thenReturn("methodName");
        when(configuration.actionSpEL()).thenReturn("methodSignature.name == 'methodName'");

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    @Test
    void whenActionSpELIsEvaluatedConfigurationIsPresentInRoot() {
        when(configuration.action()).thenReturn("my-action");
        when(configuration.actionSpEL()).thenReturn("configuration.action == 'my-action'");

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    @Test
    void whenActionSpELIsEvaluatedBeansAreAvailableFromResolver() {
        when(configuration.actionSpEL()).thenReturn("@mockBean.value");

        final var event = businessEventFactory.createBusinessEvent("payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("mockBeanValue");
    }

    @Test
    void givenANonUnwrappedPayloadWhenActionSpELIsEvaluatedWrappedPayloadIsEqualToPayload() {
        when(configuration.actionSpEL()).thenReturn("wrappedPayload == 'payload'");

        final var event = businessEventFactory.createBusinessEvent("payload", "payload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    @Test
    void givenAnUnwrappedPayloadWhenActionSpELIsEvaluatedWrappedPayloadIsNotEqualToPayload() {
        when(configuration.actionSpEL()).thenReturn("payload == 'payload' && wrappedPayload == 'wrappedPayload'");

        final var event = businessEventFactory.createBusinessEvent("payload", "wrappedPayload", this, methodSignature, configuration);
        assertThat(event.getAction()).isEqualTo("true");
    }

    public record PayloadWithDynamicAction(String action) {
    }

    public String getCalledBySpELInTest() {
        return "was called";
    }

    public static class MockBean {
        public String getValue() {
            return "mockBeanValue";
        }
    }
}