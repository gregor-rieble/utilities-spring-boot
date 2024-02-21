package de.gcoding.boot.diagnostics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosableExceptionTest {
    @ParameterizedTest
    @MethodSource("getFactoriesForDescriptionAndAction")
    void whenDescriptionAndActionArePassedTheyAreUsedForDiagnosisDetails(BiFunction<String, String, DiagnosableException> factory) {
        final var exception = factory.apply("description", "action");

        assertThat(exception.getDiagnosisDetails().description()).isEqualTo("description");
        assertThat(exception.getDiagnosisDetails().action()).isEqualTo("action");
    }

    @ParameterizedTest
    @MethodSource("getFactoriesForDescriptionAndAction")
    void whenDescriptionIsPassedItIsUsedAsExceptionMessage(BiFunction<String, String, DiagnosableException> factory) {
        final var exception = factory.apply("description", "action");

        assertThat(exception.getMessage()).isEqualTo("description");
    }

    @ParameterizedTest
    @MethodSource("getFactoriesForCause")
    void whenCauseIsSuppliedItCanBeRetrieved(Function<Exception, DiagnosableException> factory) {
        final var cause = new IllegalStateException("cause");
        final var exception = factory.apply(cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }

    static Stream<BiFunction<String, String, DiagnosableException>> getFactoriesForDescriptionAndAction() {
        return Stream.of(
            DiagnosableException::new,
            (description, action) -> new DiagnosableException(description, action, new IllegalStateException()),
            (description, action) -> new DiagnosableException(new DiagnosisDetails(description, action)),
            (description, action) -> new DiagnosableException(new DiagnosisDetails(description, action), new IllegalStateException())
        );
    }

    static Stream<Function<Exception, DiagnosableException>> getFactoriesForCause() {
        return Stream.of(
            (cause) -> new DiagnosableException("description", "action", cause),
            (cause) -> new DiagnosableException(new DiagnosisDetails("description", "action"), cause)
        );
    }
}