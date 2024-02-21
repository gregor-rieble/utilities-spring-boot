package de.gcoding.boot.businessevents.emission.unwrapper;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CollectionUnwrapperTest {
    static final String[] VALUES = new String[]{"first", "second", "third"};
    @Mock
    MethodSignature methodSignature;
    @Mock
    EmitBusinessEvent configuration;
    CollectionUnwrapper unwrapper = new CollectionUnwrapper();

    static List<Collection<String>> getCollectionImplementations() {
        return List.of(
            List.of(VALUES),
            Set.of(VALUES),
            new ArrayDeque<>(List.of(VALUES))
        );
    }

    static List<Object> getUnsupportedTypes() {
        return List.of(
            Arrays.stream(VALUES),
            List.of(VALUES).iterator(),
            Arrays.stream(VALUES).spliterator(),
            VALUES
        );
    }

    @ParameterizedTest
    @MethodSource("getCollectionImplementations")
    void whenCollectionIsPassedValuesAreUnwrapped(Collection<String> value) {
        final var result = unwrapper.unwrap(value, this, methodSignature, configuration);
        assertThat(result).hasValueSatisfying(v -> assertThat(v).containsExactlyInAnyOrder((Object[]) VALUES));
    }

    @ParameterizedTest
    @MethodSource("getUnsupportedTypes")
    void whenUnsupportedTypeIsPassedValueIsKeptUntouched(Object value) {
        final var result = unwrapper.unwrap(value, this, methodSignature, configuration);
        assertThat(result).isEmpty();
    }
}