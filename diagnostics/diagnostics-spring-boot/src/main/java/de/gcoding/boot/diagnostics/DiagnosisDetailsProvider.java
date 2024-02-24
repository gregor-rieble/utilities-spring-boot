package de.gcoding.boot.diagnostics;

/**
 * Has information on why a spring boot application could not be started by providing the {@link DiagnosisDetails}
 * through the {@link #getDiagnosisDetails()} method
 */
@FunctionalInterface
public interface DiagnosisDetailsProvider {
    /**
     * Returns the information on why the spring boot application could not start properly. The {@link DiagnosisDetails}
     * consist of a description on why the application failed to start as well as suggested action(s) on how to solve
     * the startup failure.
     *
     * @return The details on why the application failed to start and how to solve the startup failure
     */
    DiagnosisDetails getDiagnosisDetails();
}
