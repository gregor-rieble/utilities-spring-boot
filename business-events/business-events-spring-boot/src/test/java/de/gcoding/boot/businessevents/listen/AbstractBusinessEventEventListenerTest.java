package de.gcoding.boot.businessevents.listen;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.EventActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationListener;

@ExtendWith(MockitoExtension.class)
class AbstractBusinessEventEventListenerTest {
    @Spy
    MockBusinessEventEventListener delegateListener = new MockBusinessEventEventListener();
    MockEntity entity = new MockEntity();
    ApplicationListener<BusinessEvent> eventListenerUnderTest;

    @BeforeEach
    void beforeEach() {
        eventListenerUnderTest = new MockBusinessEventEventListener(delegateListener);
    }

    @Test
    void whenEventHasCreateActionOnActionIsCalled() {
        final var event = givenAnEventWithPayloadAndAction(entity, EventActions.CREATE);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onCreate(entity, event);
        Mockito.verify(delegateListener, Mockito.times(0)).onUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onDelete(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onCustom(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUnhandledAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void whenEventHasUpdateActionOnUpdateIsCalled() {
        final var event = givenAnEventWithPayloadAndAction(entity, EventActions.UPDATE);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onUpdate(entity, event);
        Mockito.verify(delegateListener, Mockito.times(0)).onCreate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onDelete(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onCustom(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUnhandledAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void whenEventHasDeleteActionOnDeleteIsCalled() {
        final var event = givenAnEventWithPayloadAndAction(entity, EventActions.DELETE);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onDelete(entity, event);
        Mockito.verify(delegateListener, Mockito.times(0)).onCreate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onCustom(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUnhandledAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.NONE, "unknown"})
    void whenEventHasUnhandledActionOnUnhandledActionIsCalled(String action) {
        final var event = givenAnEventWithPayloadAndAction(entity, action);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onUnhandledAction(action, entity, event);
        Mockito.verify(delegateListener, Mockito.times(0)).onCreate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onDelete(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onCustom(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void whenEventHasCustomActionOnCustomIsCalled() {
        final var event = givenAnEventWithPayloadAndAction(entity, "CUSTOM");
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onCustom(entity, event);
        Mockito.verify(delegateListener, Mockito.times(0)).onCreate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onDelete(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUnhandledAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void whenEventHasUnsupportedPayloadItIsNotHandled() {
        final var event = givenAnEventWithPayloadAndAction("string payload", EventActions.CREATE);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener, Mockito.times(0)).onCreate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onDelete(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onCustom(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(delegateListener, Mockito.times(0)).onUnhandledAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void whenEventPayloadIsSubtypeOfDesiredPayloadTypeItIsStillHandled() {
        final var payload = new ExtendedMockEntity();
        final var event = givenAnEventWithPayloadAndAction(payload, EventActions.CREATE);
        eventListenerUnderTest.onApplicationEvent(event);

        Mockito.verify(delegateListener).onCreate(payload, event);
    }

    private BusinessEvent givenAnEventWithPayloadAndAction(Object payload, String action) {
        return BusinessEvent.withPayload(payload)
            .action(action)
            .build();
    }

    public static class MockEntity {
    }

    public static class ExtendedMockEntity extends MockEntity {
    }

    public static class MockBusinessEventEventListener extends AbstractBusinessEventEventListener<MockEntity> {
        private final MockBusinessEventEventListener delegate;

        public MockBusinessEventEventListener() {
            this(null);
        }

        public MockBusinessEventEventListener(MockBusinessEventEventListener delegate) {
            super(MockEntity.class);
            this.delegate = delegate;

            registerCallback(this::onCustom, "CUSTOM");
        }

        protected void onCustom(MockEntity entity, BusinessEvent event) {
            if (delegate != null) {
                delegate.onCustom(entity, event);
            }
        }

        @Override
        protected void onCreate(MockEntity entity, BusinessEvent event) {
            if (delegate != null) {
                delegate.onCreate(entity, event);
            }
        }

        @Override
        protected void onUpdate(MockEntity entity, BusinessEvent event) {
            if (delegate != null) {
                delegate.onUpdate(entity, event);
            }
        }

        @Override
        protected void onDelete(MockEntity entity, BusinessEvent event) {
            if (delegate != null) {
                delegate.onDelete(entity, event);
            }
        }

        @Override
        protected void onUnhandledAction(String action, MockEntity entity, BusinessEvent event) {
            if (delegate != null) {
                delegate.onUnhandledAction(action, entity, event);
            }
        }
    }
}