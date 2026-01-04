package de.gcoding.boot.businessevents.emission;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import jakarta.annotation.Nonnull;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessEventsFactoryImplTest {
    static final String STRING_PAYLOAD = "payload";
    static final Optional<String> WRAPPED_STRING_PAYLOAD = Optional.of(STRING_PAYLOAD);
    @Mock
    MethodSignature methodSignature;
    @Mock
    EmitBusinessEvent configuration;
    @Mock
    EventPayloadUnwrapper eventPayloadUnwrapper;
    @Spy
    BusinessEventFactory businessEventFactory = new MockBusinessEventFactory();
    @InjectMocks
    BusinessEventsFactoryImpl businessEventsFactory;

    @Test
    void whenUnwrapperDidNotUnwrapAnythingASingleEventIsCreated() {
        when(eventPayloadUnwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration)).thenReturn(Optional.empty());

        final var events = businessEventsFactory.createBusinessEvents(STRING_PAYLOAD, this, methodSignature, configuration);

        assertThat(events).hasSize(1).allSatisfy(event -> assertThat(event.getPayload()).isEqualTo(STRING_PAYLOAD));
    }

    @Test
    void whenUnwrappingShouldBeSkippedASingleEventIsCreated() {
        when(configuration.skipUnwrap()).thenReturn(true);

        final var events = businessEventsFactory.createBusinessEvents(STRING_PAYLOAD, this, methodSignature, configuration);

        assertThat(events).hasSize(1).allSatisfy(event -> assertThat(event.getPayload()).isEqualTo(STRING_PAYLOAD));
        verify(eventPayloadUnwrapper, times(0)).unwrap(STRING_PAYLOAD, this, methodSignature, configuration);
    }

    @Test
    void whenMultipleValuesAreUnwrappedAnEventIsCreatedForEachOfThem() {
        when(eventPayloadUnwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration))
            .thenReturn(Optional.of(Stream.of("first", "second", "third")));

        final var events = businessEventsFactory.createBusinessEvents(STRING_PAYLOAD, this, methodSignature, configuration);

        assertThat(events).hasSize(3).allSatisfy(event -> assertThat(Set.of("first", "second", "third")).contains(event.getPayloadAs(String.class)));
    }

    @Test
    void whenPayloadIsNullNoEventIsGenerated() {
        final var events = businessEventsFactory.createBusinessEvents(null, this, methodSignature, configuration);

        assertThat(events).isEmpty();
    }

    @Test
    void whenPayloadInNotUnwrappedBusinessEventFactoryIsCalledWithPayloadForWrappedAndNonWrappedPayloadParameters() {
        when(eventPayloadUnwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration)).thenReturn(Optional.empty());

        businessEventsFactory.createBusinessEvents(STRING_PAYLOAD, this, methodSignature, configuration);

        verify(businessEventFactory).createBusinessEvent(STRING_PAYLOAD, STRING_PAYLOAD, this, methodSignature, configuration);
    }

    @Test
    void whenPayloadInUnwrappedBusinessEventFactoryIsCalledWithOriginalPayloadForWrappedPayloadAndUnwrappedPayloadForUnwrappedPayloadParameters() {
        when(eventPayloadUnwrapper.unwrap(WRAPPED_STRING_PAYLOAD, this, methodSignature, configuration)).thenReturn(Optional.of(Stream.of(STRING_PAYLOAD)));

        businessEventsFactory.createBusinessEvents(WRAPPED_STRING_PAYLOAD, this, methodSignature, configuration);

        verify(businessEventFactory).createBusinessEvent(STRING_PAYLOAD, WRAPPED_STRING_PAYLOAD, this, methodSignature, configuration);
    }

    protected static class MockBusinessEventFactory implements BusinessEventFactory {
        @Override
        public @Nonnull BusinessEvent createBusinessEvent(@Nonnull Object payload, @Nonnull Object wrappedPayload, @Nonnull Object emittingSource, @Nonnull MethodSignature methodSignature, @Nonnull EmitBusinessEvent configuration) {
            return BusinessEvent.withPayload(payload).build();
        }
    }
}