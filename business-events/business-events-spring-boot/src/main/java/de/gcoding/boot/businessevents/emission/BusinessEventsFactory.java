package de.gcoding.boot.businessevents.emission;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.List;

@FunctionalInterface
public interface BusinessEventsFactory {
    @Nonnull
    List<BusinessEvent> createBusinessEvents(
        @Nullable Object payload,
        @Nonnull Object emittingSource,
        @Nonnull MethodSignature methodSignature,
        @Nonnull EmitBusinessEvent configuration
    );
}
