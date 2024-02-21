package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CompositeEventPayloadUnwrapperTest {
    static final String STRING_PAYLOAD = "payload";
    @Mock
    MethodSignature methodSignature;
    @Mock
    EmitBusinessEvent configuration;

    @Test
    void whenEmptyListIsGivenNoUnwrappingTakesPlace() {
        final var unwrapper = new CompositeEventPayloadUnwrapper(List.of());
        final var result = unwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration);

        assertThat(result).isEmpty();
    }

    @Test
    void whenNoUnwrapperIsAbleToUnwrapNoUnwrappingTakesPlace() {
        final var unwrapper = new CompositeEventPayloadUnwrapper(List.of(
            (p, s, m, c) -> Optional.empty(),
            (p, s, m, c) -> Optional.empty()
        ));
        final var result = unwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration);
        assertThat(result).isEmpty();
    }

    @Test
    void whenUnwrapperIsAbleToUnwrapPayloadOthersAreSkipped() {
        final var unwrapper = new CompositeEventPayloadUnwrapper(List.of(
            (p, s, m, c) -> Optional.empty(),
            (p, s, m, c) -> Optional.of(Stream.of("unwrapped")),
            (p, s, m, c) -> {
                throw new IllegalStateException("should not be executed");
            }
        ));
        final var result = unwrapper.unwrap(STRING_PAYLOAD, this, methodSignature, configuration);
        assertThat(result).hasValueSatisfying(value -> assertThat(value).containsExactly("unwrapped"));
    }
}