package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Unwraps payloads of {@link Collection} types by returning a stream that contains an entry for each
 * element of the given collection. This results in events being emitted for each single element. If
 * the given payload is not a collection, this unwrapper abstains from unwrapping the payload by returning
 * an empty optional
 */
public class CollectionUnwrapper implements EventPayloadUnwrapper {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NonNull Optional<Stream<Object>> unwrap(
        @Nullable Object payload,
        @NonNull Object emittingSource,
        @NonNull MethodSignature methodSignature,
        @NonNull EmitBusinessEvent configuration
    ) {
        if (payload instanceof Collection collection) {
            return Optional.of(collection.stream());
        }

        return Optional.empty();
    }
}
