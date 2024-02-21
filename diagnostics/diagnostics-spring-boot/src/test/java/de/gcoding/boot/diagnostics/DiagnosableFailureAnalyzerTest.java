package de.gcoding.boot.diagnostics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosableFailureAnalyzerTest {
    DiagnosableFailureAnalyzer failureAnalyzer;

    @BeforeEach
    void beforeEach() {
        failureAnalyzer = new DiagnosableFailureAnalyzer();
    }

    @Test
    void whenNoDiagnosableExceptionIsInStackTraceNullIsReturned() {
        final var stackTrace = new IllegalStateException(new RuntimeException(new IOException()));

        final var result = failureAnalyzer.analyze(stackTrace);

        assertThat(result).isNull();
    }

    @Test
    void detailsExtractorIsExecutedForEachNonDiagnosableExceptionInStack() {
        final var analyzed = new LinkedList<>();
        failureAnalyzer.diagnosisDetailsExtractor = (error) -> {
            analyzed.add(error);
            return Optional.empty();
        };

        final var failure = new IllegalStateException(new IllegalArgumentException(new IOException()));
        final var expected = List.of(failure, failure.getCause(), failure.getCause().getCause());

        failureAnalyzer.analyze(failure);

        assertThat(analyzed).isEqualTo(expected);
    }

    @Test
    void detailsExtractorIsExecutedUntilFirstDiagnosableExceptionIsFound() {
        final var analyzed = new LinkedList<>();
        failureAnalyzer.diagnosisDetailsExtractor = (error) -> {
            analyzed.add(error);
            if (error instanceof IllegalArgumentException) {
                return Optional.of(new DiagnosisDetails("found", "details"));
            }

            return Optional.empty();
        };

        final var failure = new IllegalStateException(new IllegalArgumentException(new IOException(new DiagnosableException("should not be", "found"))));
        final var expected = List.of(failure, failure.getCause());

        failureAnalyzer.analyze(failure);

        assertThat(analyzed).isEqualTo(expected);
    }

    @Test
    void detailsAreExtractedFromAnnotatedException() {
        final var result = failureAnalyzer.analyze(new AnnotatedDiagnosableException());

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("@Diagnosable");
        assertThat(result.getAction()).isEqualTo("AnnotatedDiagnosableException");
    }

    @Test
    void detailsAreExtractedFromExtendingException() {
        final var result = failureAnalyzer.analyze(new ExtendingDiagnosableException());

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("extends");
        assertThat(result.getAction()).isEqualTo("ExtendingDiagnosableException");
    }

    @Test
    void detailsAreExtractedFromImplementingException() {
        final var result = failureAnalyzer.analyze(new ImplementingDiagnosableException());

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("implements");
        assertThat(result.getAction()).isEqualTo("ImplementingDiagnosableException");
    }

    @Test
    void causeIsTheDiagnosableException() {
        final var diagnosable = new AnnotatedDiagnosableException();
        final var result = failureAnalyzer.analyze(new IllegalStateException(diagnosable));

        assertThat(result.getCause()).isSameAs(diagnosable);
    }

    @Diagnosable(description = "@Diagnosable", action = "AnnotatedDiagnosableException")
    public static class AnnotatedDiagnosableException extends RuntimeException {
    }

    public static class ExtendingDiagnosableException extends DiagnosableException {
        public ExtendingDiagnosableException() {
            super("extends", "ExtendingDiagnosableException");
        }
    }

    public static class ImplementingDiagnosableException extends RuntimeException implements DiagnosisDetailsProvider {
        @Override
        public DiagnosisDetails getDiagnosisDetails() {
            return new DiagnosisDetails("implements", "ImplementingDiagnosableException");
        }
    }
}