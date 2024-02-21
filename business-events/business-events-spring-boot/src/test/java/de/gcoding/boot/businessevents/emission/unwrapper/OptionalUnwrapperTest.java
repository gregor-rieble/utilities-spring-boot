package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OptionalUnwrapperTest {
    @Mock
    MethodSignature methodSignature;
    @Mock
    EmitBusinessEvent configuration;
    OptionalUnwrapper unwrapper = new OptionalUnwrapper();

    @Test
    void whenOptionalIsPassedValueIsUnwrapped() {
        final var wrappedValue = Optional.of("value");
        final var result = unwrapper.unwrap(wrappedValue, this, methodSignature, configuration);

        assertThat(result).hasValueSatisfying(v -> assertThat(v).containsExactly("value"));
    }

    @Test
    void whenNoOptionalIsPassedValueIsNotTouched() {
        final var nonOptionalValue = List.of("test", "other");
        final var result = unwrapper.unwrap(nonOptionalValue, this, methodSignature, configuration);

        assertThat(result).isEmpty();
    }
}