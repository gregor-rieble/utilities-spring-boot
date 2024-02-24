package de.gcoding.boot.diagnostics;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 * Specifies details on why a spring boot application could not be started. If used in conjunction with
 * {@link Diagnosable}, {@link DiagnosisDetailsProvider} or {@link DiagnosableException}, spring boot will render
 * a helpful text on why the application failed to start using the description and action that was specified.
 * </p>
 * <p>
 * E.g., given the description {@code "Your application could not be started"} and the action
 * {@code "Make sure to use the correct Java version"}, the following text can be rendered in case of startup
 * failures in your spring boot application:
 * </p>
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
 *
 * @param description The description on why the application could not be started
 * @param action      The suggested action(s) on how to solve the startup failure
 */
public record DiagnosisDetails(String description, String action) implements Serializable {
    /**
     * Start building {@link DiagnosisDetails} with the given description
     *
     * @param description The description on why the application failed to start
     * @return A builder that can be used to add one or more suggested actions on how to solve the startup failure
     */
    public static DiagnosisDetailsBuilder withDescription(String description) {
        return new DiagnosisDetailsBuilder(description);
    }

    /**
     * Allows to build {@link DiagnosisDetails} with a fluent API and helpers for adding multiple suggested
     * actions
     */
    public static class DiagnosisDetailsBuilder {
        public static final String DEFAULT_MULTI_OPTION_PREAMBLE = "You can take the following actions to solve the issue:";
        private final String description;

        protected DiagnosisDetailsBuilder(String description) {
            this.description = description;
        }

        /**
         * Creates {@link DiagnosisDetails} using the given {@code action} as the only suggested action for
         * solving the startup failure
         *
         * @param action The suggested action on how to solve the startup failure
         * @return The resulting {@link DiagnosisDetails} with the given {@code action}
         */
        public DiagnosisDetails andSuggestedAction(String action) {
            return new DiagnosisDetails(description, action);
        }

        /**
         * <p>
         * Returns a {@link DiagnosisOptionsBuilder} that will produce a list containing all the suggested actions
         * added to it in separate lines. The list will be preceded by the default text
         * {@code "You can take the following actions to solve the issue"}
         * </p>
         * <p>
         * When the diagnosis details will be finally built, the suggested actions text will look like this:
         * </p>
         * <pre>
         * You can take the following actions to solve the issue:
         *     - Option 1 to solve
         *     - Option 2 to solve
         * </pre>
         *
         * @return A DiagnosisOptionsBuilder that can be used to add multiple suggested actions to solve the startup failure
         */
        public DiagnosisOptionsBuilder andSuggestedActions() {
            return andSuggestedActions(DEFAULT_MULTI_OPTION_PREAMBLE);
        }

        /**
         * <p>
         * Returns a {@link DiagnosisOptionsBuilder} that will produce a list containing all the suggested actions
         * added to it in separate lines. The list will be preceded by the given {@code preambleText}.
         * </p>
         * <p>
         * Given a {@code preambleText} of {@code "Please check the following:"}, then when the diagnosis details will be
         * finally built, the suggested actions text will look like this:
         * </p>
         * <pre>
         * Please check the following:
         *     - Option 1 to solve
         *     - Option 2 to solve
         * </pre>
         *
         * @return A DiagnosisOptionsBuilder that can be used to add multiple suggested actions to solve the startup failure
         */
        public DiagnosisOptionsBuilder andSuggestedActions(String preambleText) {
            return new DiagnosisOptionsBuilder(description, preambleText);
        }
    }

    /**
     * <p>
     * Allows you to build {@link DiagnosisDetails} that contain a action text with multiple suggested actions on how
     * to solve spring boot startup failures. The suggested actions will be rendered as a list containing each
     * option in a separate line as well as a preamble that will be rendered in front of the list (also in a separate line).
     * </p>
     * <p>Example:</p>
     * <pre>
     * You can take the following actions to solve the issue:
     *     - Option 1 to solve
     *     - Option 2 to solve
     * </pre>
     */
    public static class DiagnosisOptionsBuilder {
        private final String description;
        private final String preambleText;
        private final List<Supplier<Optional<String>>> options = new LinkedList<>();

        protected DiagnosisOptionsBuilder(String description, String preambleText) {
            this.description = description;
            this.preambleText = preambleText;
        }

        /**
         * Adds the given {@code suggestedAction} to the list of suggested actions that can be taken to solve the
         * startup failure of the application.
         *
         * @param suggestedAction An additional suggested action to take
         * @return The builder that can be used to continue adding suggested actions
         */
        public DiagnosisOptionsBuilder of(String suggestedAction) {
            return of(() -> Optional.of(suggestedAction));
        }

        /**
         * <p>
         * Conditionally adds a suggested action to the list of suggested actions that can be taken to solve the
         * startup failure of the application.
         * </p>
         * <p>
         * No action will be added, if the {@code Optional} returned by {@code conditionallyAvailableSuggestedAction}
         * is empty. Otherwise, the value contained in the returned {@code Optional} will be used as a suggested
         * action without modification.
         * </p>
         * <p>
         * This allows you to only add actions that might be available under specific circumstances but still using
         * the fluent api of this builder.
         * </p>
         *
         * @param conditionallyAvailableSuggestedAction The action that might only be available under some circumstances
         * @return The builder that can be used to continue adding suggested actions
         */
        public DiagnosisOptionsBuilder of(Supplier<Optional<String>> conditionallyAvailableSuggestedAction) {
            options.add(conditionallyAvailableSuggestedAction);
            return this;
        }

        /**
         * Convenience for
         * <pre>
         * of(suggestedAction).build()
         * </pre>
         *
         * @param suggestedAction An additional suggested action on how to solve the startup failure
         * @return The resulting diagnosis details
         * @see #of(String)
         * @see #build()
         */
        public DiagnosisDetails and(String suggestedAction) {
            of(suggestedAction);
            return build();
        }

        /**
         * Convenience for
         * <pre>
         * of(conditionallyAvailableSuggestedAction).build()
         * </pre>
         *
         * @param conditionallyAvailableSuggestedAction The action that might only be available under some circumstances
         * @return The resulting diagnosis details
         * @see #of(Supplier)
         * @see #build()
         */
        public DiagnosisDetails and(Supplier<Optional<String>> conditionallyAvailableSuggestedAction) {
            of(conditionallyAvailableSuggestedAction);
            return build();
        }

        /**
         * Creates {@link DiagnosisDetails} having the added suggested actions as a rendered list and the preamble text
         * in front of it as a action text.
         *
         * @return The details containing the desired suggested actions on how to solve the startup failure
         */
        public DiagnosisDetails build() {
            final var actionBuilder = new StringBuilder();

            actionBuilder.append(preambleText.trim());
            actionBuilder.append('\n');

            options.stream()
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(option -> {
                    actionBuilder.append("\t- ");
                    actionBuilder.append(option);
                    actionBuilder.append('\n');
                });

            final var action = actionBuilder.toString().trim();
            return new DiagnosisDetails(description, action);
        }
    }
}
