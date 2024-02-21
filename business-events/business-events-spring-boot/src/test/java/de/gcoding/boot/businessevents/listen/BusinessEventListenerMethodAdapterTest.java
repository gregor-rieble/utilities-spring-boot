package de.gcoding.boot.businessevents.listen;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.BusinessEventDataProvider;
import de.gcoding.boot.businessevents.EventActions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BusinessEventListenerMethodAdapterTest {
    BusinessEventListener configuration;
    @Spy
    MockMethods mockMethods = new MockMethods();

    @ParameterizedTest
    @ValueSource(classes = {Object.class, String.class, MockPayload.class})
    void whenMethodHasNoArgumentsItCanBeBoundWithAnyPayloadType(Class<?> payloadType) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("noArguments");
        final var event = givenAnEventWithPayloadOfType(payloadType);

        adapter.onApplicationEvent(event);

        verify(mockMethods).noArguments();
    }

    @ParameterizedTest
    @ValueSource(classes = {Object.class, String.class, MockPayload.class})
    void whenMethodHasBusinessEventParameterItCanBeBoundWithAnyPayloadType(Class<?> payloadType) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("businessEvent", BusinessEventDataProvider.class);
        final var event = givenAnEventWithPayloadOfType(payloadType);

        adapter.onApplicationEvent(event);

        verify(mockMethods).businessEvent(event);
    }

    @ParameterizedTest
    @ValueSource(classes = {MockPayload.class, ExtendedMockPayload.class})
    void whenMethodHasPayloadParameterItCanBeBoundWithAnyPayloadSubtype(Class<?> payloadType) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("mockPayload", MockPayload.class);
        final var event = givenAnEventWithPayloadOfType(payloadType);

        adapter.onApplicationEvent(event);

        verify(mockMethods).mockPayload((MockPayload) event.getPayload());
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.NONE, EventActions.CREATE, EventActions.UPDATE, EventActions.DELETE})
    void whenMethodHasActionParameterItCanBeBound(String action) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("action", String.class);
        final var event = givenAnEventWithAction(action);

        adapter.onApplicationEvent(event);

        verify(mockMethods).action(action);
    }

    @Test
    void whenMethodHasStringPayloadAndActionParameterPayloadIsBoundToFirstParameter() {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("stringPayloadAndAction", String.class, String.class);
        final var event = givenAnEventWithPayloadOfType(String.class);

        adapter.onApplicationEvent(event);

        verify(mockMethods).stringPayloadAndAction((String) event.getPayload(), event.getAction());
    }

    @Test
    void whenMethodHasAllPossibleParametersAllCanBeBound() {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("allArguments", BusinessEventDataProvider.class, MockPayload.class, String.class);
        final var event = givenAnEventWithPayloadOfType(MockPayload.class);

        adapter.onApplicationEvent(event);

        verify(mockMethods).allArguments(event, (MockPayload) event.getPayload(), event.getAction());
    }

    @Test
    void whenMethodHasAllPossibleParametersInDifferentOrderAllCanBeBound() {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("differentOrder", MockPayload.class, String.class, BusinessEventDataProvider.class);
        final var event = givenAnEventWithPayloadOfType(MockPayload.class);

        adapter.onApplicationEvent(event);

        verify(mockMethods).differentOrder((MockPayload) event.getPayload(), event.getAction(), event);
    }

    @ParameterizedTest
    @ValueSource(classes = {MockPayload.class, ExtendedMockPayload.class})
    void whenMethodHasPayloadTypeFilterItIsExecutedForPayloadsOfThatType(Class<?> payloadType) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("filterByPayloadType");
        final var event = givenAnEventWithPayloadOfType(payloadType);

        adapter.onApplicationEvent(event);

        verify(mockMethods).filterByPayloadType();
    }

    @ParameterizedTest
    @ValueSource(classes = {Object.class, String.class})
    void whenMethodHasPayloadTypeFilterItIsNotExecutedForPayloadsOfIncompatibleType(Class<?> payloadType) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("filterByPayloadType");
        final var event = givenAnEventWithPayloadOfType(payloadType);

        adapter.onApplicationEvent(event);

        verify(mockMethods, times(0)).filterByPayloadType();
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.DELETE, EventActions.CREATE})
    void whenMethodHasActionFilterItIsExecutedForDesiredActions(String action) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("filterByAction");
        final var event = givenAnEventWithAction(action);

        adapter.onApplicationEvent(event);

        verify(mockMethods).filterByAction();
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.NONE, EventActions.UPDATE})
    void whenMethodHasActionFilterItIsNotExecutedForUndesiredActions(String action) {
        final var adapter = givenAnAdapterForAnAnnotatedMethod("filterByAction");
        final var event = givenAnEventWithAction(action);

        adapter.onApplicationEvent(event);

        verify(mockMethods, times(0)).filterByAction();
    }

    @Test
    void whenMethodHasTooManyArgumentsItCannotBeBound() {
        assertThrows(
            IllegalStateException.class,
            () -> givenAnAdapterForAnAnnotatedMethod("tooManyArguments", BusinessEventDataProvider.class, Object.class, String.class, Integer.class)
        );
    }

    @Test
    void whenBusinessEventArgumentHasInvalidTypeItCannotBeBound() {
        final var error = assertThrows(
            IllegalStateException.class,
            () -> givenAnAdapterForAnAnnotatedMethod("eventCannotBeBound", StringBuilder.class, MockPayload.class, String.class)
        );

        assertThat(error).hasMessageContaining("at position 0");
    }

    @Test
    void whenPayloadArgumentHasInvalidTypeItCannotBeBound() {
        final var error = assertThrows(
            IllegalStateException.class,
            () -> givenAnAdapterForAnAnnotatedMethod("payloadCannotBeBound", BusinessEventDataProvider.class, MockPayload.class, String.class)
        );

        assertThat(error).hasMessageContaining("at position 1");
    }

    @Test
    void whenActionArgumentHasInvalidTypeItCannotBeBound() {
        final var error = assertThrows(
            IllegalStateException.class,
            () -> givenAnAdapterForAnAnnotatedMethod("actionCannotBeBound", BusinessEventDataProvider.class, MockPayload.class, StringBuilder.class)
        );

        assertThat(error).hasMessageContaining("at position 2");
    }


    private BusinessEvent givenAnEventWithPayloadOfType(Class<?> payloadType) {
        final var payload = sneakyThrows(() -> payloadType.getConstructor().newInstance());
        return BusinessEvent.withPayload(payload)
            .action(EventActions.CREATE)
            .build();
    }

    private BusinessEvent givenAnEventWithAction(String action) {
        final var payload = new MockPayload();
        return BusinessEvent.withPayload(payload)
            .action(action)
            .build();
    }

    private BusinessEventListenerMethodAdapter givenAnAdapterForAnAnnotatedMethod(String methodName, Class<?>... parameterTypes) {
        final var method = sneakyThrows(() -> MockMethods.class.getDeclaredMethod(methodName, parameterTypes));

        configuration = method.getAnnotation(BusinessEventListener.class);
        assertThat(configuration).isNotNull();

        return new BusinessEventListenerMethodAdapter(
            configuration,
            () -> mockMethods,
            method
        );
    }

    public static class MockMethods {
        @BusinessEventListener
        public void noArguments() {
        }

        @BusinessEventListener
        public void businessEvent(BusinessEventDataProvider businessEvent) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void mockPayload(MockPayload payload) {
        }

        @BusinessEventListener
        public void action(String action) {
        }

        @BusinessEventListener(payloadType = String.class)
        public void stringPayloadAndAction(String payload, String action) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void allArguments(BusinessEventDataProvider businessEvent, MockPayload payload, String action) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void differentOrder(MockPayload payload, String action, BusinessEventDataProvider businessEvent) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void filterByPayloadType() {
        }

        @BusinessEventListener(actions = {EventActions.DELETE, EventActions.CREATE})
        public void filterByAction() {
        }

        @BusinessEventListener
        public void tooManyArguments(BusinessEventDataProvider businessEvent, Object payload, String action, Integer notAllowed) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void eventCannotBeBound(StringBuilder businessEvent, MockPayload payload, String action) {
        }

        @BusinessEventListener(payloadType = String.class)
        public void payloadCannotBeBound(BusinessEventDataProvider businessEvent, MockPayload payload, String action) {
        }

        @BusinessEventListener(payloadType = MockPayload.class)
        public void actionCannotBeBound(BusinessEventDataProvider businessEvent, MockPayload payload, StringBuilder action) {
        }
    }

    public static class MockPayload {
    }

    public static class ExtendedMockPayload extends MockPayload {
    }
}