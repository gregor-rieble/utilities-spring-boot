package de.gcoding.boot.businessevents.emission;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nonnull;
import org.aspectj.lang.reflect.MethodSignature;

@FunctionalInterface
public interface BusinessEventFactory {
    @Nonnull
    default BusinessEvent createBusinessEvent(
        @Nonnull Object payload,
        @Nonnull Object emittingSource,
        @Nonnull MethodSignature methodSignature,
        @Nonnull EmitBusinessEvent configuration
    ) {
        return createBusinessEvent(payload, payload, emittingSource, methodSignature, configuration);
    }

    @Nonnull
    BusinessEvent createBusinessEvent(
        @Nonnull Object payload,
        @Nonnull Object wrappedPayload,
        @Nonnull Object emittingSource,
        @Nonnull MethodSignature methodSignature,
        @Nonnull EmitBusinessEvent configuration
    );
}
