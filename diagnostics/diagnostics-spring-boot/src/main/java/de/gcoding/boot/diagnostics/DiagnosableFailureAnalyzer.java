package de.gcoding.boot.diagnostics;

import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Optional;

/**
 * Looks for an exception implementing {@link DiagnosisDetailsProvider} or annotated with {@link Diagnosable}
 * in the stack trace to provide details on the startup failure
 */
public class DiagnosableFailureAnalyzer implements FailureAnalyzer {
    // protected for changing implementation in tests
    protected DiagnosisDetailsExtractor diagnosisDetailsExtractor = DiagnosableFailureAnalyzer::resolveDiagnosisDetails;

    @Override
    public FailureAnalysis analyze(Throwable failure) {
        if (failure == null) {
            return null;
        }

        return diagnosisDetailsExtractor.extract(failure)
            .map(details -> new FailureAnalysis(details.description(), details.action(), failure))
            .orElseGet(() -> analyze(failure.getCause()));
    }

    private static Optional<DiagnosisDetails> resolveDiagnosisDetails(Throwable failure) {
        if (failure instanceof DiagnosisDetailsProvider provider) {
            return Optional.ofNullable(provider.getDiagnosisDetails());
        }

        final var exceptionType = failure.getClass();
        final var diagnosableAnnotation = AnnotationUtils.findAnnotation(exceptionType, Diagnosable.class);
        if (diagnosableAnnotation != null) {
            return Optional.of(new DiagnosisDetails(
                diagnosableAnnotation.description(),
                diagnosableAnnotation.action()
            ));
        }

        return Optional.empty();
    }

    @FunctionalInterface
    public interface DiagnosisDetailsExtractor {
        Optional<DiagnosisDetails> extract(Throwable failure);
    }
}
