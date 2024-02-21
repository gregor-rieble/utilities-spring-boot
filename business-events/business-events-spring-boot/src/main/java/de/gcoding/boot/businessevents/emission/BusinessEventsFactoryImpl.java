package de.gcoding.boot.businessevents.emission;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class BusinessEventsFactoryImpl implements BusinessEventsFactory {
    private final EventPayloadUnwrapper eventPayloadUnwrapper;
    private final BusinessEventFactory businessEventFactory;

    public BusinessEventsFactoryImpl(@NonNull EventPayloadUnwrapper eventPayloadUnwrapper, @NonNull BusinessEventFactory businessEventFactory) {
        this.eventPayloadUnwrapper = requireNonNull(eventPayloadUnwrapper);
        this.businessEventFactory = requireNonNull(businessEventFactory);
    }

    @Override
    @NonNull
    public List<BusinessEvent> createBusinessEvents(
        @Nullable Object payload,
        @NonNull Object emittingSource,
        @NonNull MethodSignature methodSignature,
        @NonNull EmitBusinessEvent configuration
    ) {
        if (payload != null) {
            return unwrapEventPayloads(payload, emittingSource, methodSignature, configuration)
                .map(singlePayload -> businessEventFactory.createBusinessEvent(singlePayload, emittingSource, methodSignature, configuration))
                .toList();
        }

        return List.of();
    }

    private Stream<Object> unwrapEventPayloads(
        Object payload,
        Object emittingSource,
        MethodSignature methodSignature,
        EmitBusinessEvent configuration
    ) {
        if (configuration.skipUnwrap()) {
            return Stream.of(payload);
        }

        return eventPayloadUnwrapper.unwrap(payload, emittingSource, methodSignature, configuration)
            .orElseGet(() -> Stream.of(payload));
    }
}
