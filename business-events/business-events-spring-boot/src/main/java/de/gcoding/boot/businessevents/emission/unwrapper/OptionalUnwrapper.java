package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Unwraps payloads of type {@link Optional}. If the optional is empty, an empty stream will be returned,
 * which means that no events will be emitted. If a value is present within the optional, the value itself
 * will be present as a single entry in the returned stream. If the payload is not an optional, this unwrapper
 * abstains from unwrapping by returning an empty optional
 */
public class OptionalUnwrapper implements EventPayloadUnwrapper {
    @Override
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<Stream<Object>> unwrap(
        @Nullable Object payload,
        @NonNull Object emittingSource,
        @NonNull MethodSignature methodSignature,
        @NonNull EmitBusinessEvent configuration
    ) {
        if (payload instanceof Optional optional) {
            return Optional.of(optional.stream());
        }

        return Optional.empty();
    }
}
