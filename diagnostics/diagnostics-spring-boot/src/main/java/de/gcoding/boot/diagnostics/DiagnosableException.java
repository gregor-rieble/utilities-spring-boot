package de.gcoding.boot.diagnostics;

/**
 * Exceptions of this type mark spring boot application startup errors that can be presented to the user in a
 * human-readable, user-friendly way. For example, if a {@code DiagnosableException} is encountered during
 * application start, you might get the following output in your logs:
 * <pre>
 * ***************************
 * APPLICATION FAILED TO START
 * ***************************
 *
 * Description:
 *
 * Your application could not be started
 *
 * Action:
 *
 * Make sure to use the correct Java version
 * </pre>
 * This holds true, if the description of the {@code DiagnosableException} was set to
 * {@code Your application could not be started} and the action to {@code Make sure to use the correct Java version}
 */
public class DiagnosableException extends RuntimeException implements DiagnosisDetailsProvider {
    private final DiagnosisDetails diagnosisDetails;

    /**
     * Creates a new {@link DiagnosableException} with the given {@code description} and {@code action}
     *
     * @param description The description on why the application failed to start
     * @param action      The suggested action(s) needed to solve the startup failure
     */
    public DiagnosableException(String description, String action) {
        this(description, action, null);
    }

    /**
     * Creates a new {@link DiagnosableException} with the given {@code description} and {@code action}
     *
     * @param description The description on why the application failed to start
     * @param action      The suggested action(s) needed to solve the startup failure
     * @param cause       The root cause of the startup failure
     */
    public DiagnosableException(String description, String action, Throwable cause) {
        this(new DiagnosisDetails(description, action), cause);
    }

    /**
     * Creates a new {@link DiagnosableException} with the given {@link DiagnosisDetails}
     *
     * @param diagnosisDetails The description and action(s) on why the application failed to start and how to
     *                         solve the startup failure
     */
    public DiagnosableException(DiagnosisDetails diagnosisDetails) {
        this(diagnosisDetails, null);
    }

    /**
     * Creates a new {@link DiagnosableException} with the given {@link DiagnosisDetails}
     *
     * @param diagnosisDetails The description and action(s) on why the application failed to start and how to
     *                         solve the startup failure
     * @param cause            The root cause of the startup failure
     */
    public DiagnosableException(DiagnosisDetails diagnosisDetails, Throwable cause) {
        super(diagnosisDetails.description(), cause);
        this.diagnosisDetails = diagnosisDetails;
    }

    @Override
    public DiagnosisDetails getDiagnosisDetails() {
        return diagnosisDetails;
    }
}
