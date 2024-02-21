package de.gcoding.boot.diagnostics;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public record DiagnosisDetails(String description, String action) {
    public static DiagnosisDetailsBuilder withDescription(String description) {
        return new DiagnosisDetailsBuilder(description);
    }

    public static class DiagnosisDetailsBuilder {
        private final String description;

        protected DiagnosisDetailsBuilder(String description) {
            this.description = description;
        }

        public DiagnosisDetails andActionToSolveTheIssue(String action) {
            return new DiagnosisDetails(description, action);
        }

        public DiagnosisOptionsBuilder andMultipleOptionsOnHowToSolveTheIssue() {
            return andMultipleOptionsOnHowToSolveTheIssue("You can take the following actions to solve the issue:");
        }

        public DiagnosisOptionsBuilder andMultipleOptionsOnHowToSolveTheIssue(String preambleText) {
            return new DiagnosisOptionsBuilder(description, preambleText);
        }
    }

    public static class DiagnosisOptionsBuilder {
        private final String description;
        private final String preambleText;
        private final List<Supplier<Optional<String>>> options = new LinkedList<>();

        protected DiagnosisOptionsBuilder(String description, String preambleText) {
            this.description = description;
            this.preambleText = preambleText;
        }

        public DiagnosisOptionsBuilder withOption(String option) {
            return withOption(() -> Optional.of(option));
        }

        public DiagnosisOptionsBuilder withOption(Supplier<Optional<String>> optionSupplier) {
            options.add(optionSupplier);
            return this;
        }

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
