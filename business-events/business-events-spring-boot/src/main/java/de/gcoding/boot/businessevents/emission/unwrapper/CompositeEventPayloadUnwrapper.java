package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Contains a list of child unwrapper instances. Will go through each child unwrapper in an ordered fashion
 * and tries to unwrap the event payload. The return value from the first child unwrapper that does not return
 * an empty optional will be used as a result.
 */
public class CompositeEventPayloadUnwrapper implements EventPayloadUnwrapper {
    private final List<EventPayloadUnwrapper> delegates;

    /**
     * Creates a new {@link CompositeEventPayloadUnwrapper}.
     *
     * @param delegates The child unwrapper instances to be used. Order of unwrapper instances in this list is honored
     *                  during the unwrap process
     */
    public CompositeEventPayloadUnwrapper(@NonNull List<EventPayloadUnwrapper> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Override
    @NonNull
    public Optional<Stream<Object>> unwrap(
        @Nullable Object payload,
        @NonNull Object emittingSource,
        @NonNull MethodSignature methodSignature,
        @NonNull EmitBusinessEvent configuration
    ) {
        for (final var unwrapper : delegates) {
            final var result = unwrapper.unwrap(payload, emittingSource, methodSignature, configuration);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }
}
