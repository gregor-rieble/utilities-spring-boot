package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Allows to unwrap event payloads returned by @{@link EmitBusinessEvent} annotated methods prior to emission
 */
@FunctionalInterface
public interface EventPayloadUnwrapper {
    EventPayloadUnwrapper NOOP = (payload, source, method, config) -> Optional.empty();

    /**
     * Unwraps the given {@code payload} into 0 to {@code n} new payloads from which events will be created.
     * If this unwrapper does not support the given {@code payload}, it will return an empty Optional
     * hinting that the next unwrapper implementation might go ahead and try to unwrap the payload. If the payload
     * can be unwrapped, the resulting Optional will contain a Stream of unwrapped payloads. For each of those payloads
     * a {@link BusinessEvent} will then be emitted.
     *
     * @param payload         The payload that should be unwrapped
     * @param emittingSource  The instance owning the @EmitBusinessEvent annotated method that was invoked
     * @param methodSignature The method signature of the method that was annotated with @EmitBusinessEvent
     * @param configuration   The concrete values of the @EmitBusinessEvent annotation that was used
     * @return The unwrapped payloads from which to create events or an empty optional in case this unwrapper does
     * not support unwrapping for the given payload
     */
    @Nonnull
    Optional<Stream<Object>> unwrap(
        @Nullable Object payload,
        @Nonnull Object emittingSource,
        @Nonnull MethodSignature methodSignature,
        @Nonnull EmitBusinessEvent configuration
    );
}
