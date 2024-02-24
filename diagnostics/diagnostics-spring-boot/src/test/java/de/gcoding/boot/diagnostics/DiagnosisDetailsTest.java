package de.gcoding.boot.diagnostics;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosisDetailsTest {
    @Test
    void simpleDiagnosisDetailsCanBeCreatedThroughBuilder() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedAction("action");

        assertThat(details.description()).isEqualTo("description");
        assertThat(details.action()).isEqualTo("action");
    }

    @Test
    void multipleActionsCanBeAdded() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .of(() -> Optional.of("second"))
            .of("third")
            .build();

        assertThat(details.action())
            .matches("(?s).*\\bfirst\\b.*")
            .matches("(?s).*\\bsecond\\b.*")
            .matches("(?s).*\\bthird\\b.*");
    }

    @Test
    void eachActionIsRenderedInSeparateLine() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .of("second")
            .of("third")
            .build();

        assertThat(details.action()).hasLineCount(4); // 1 preamble line and 3 actions
    }

    @Test
    void emptyOptionalInMultiActionsIsNotRendered() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .of(Optional::empty)
            .of("third")
            .build();

        assertThat(details.action())
            .hasLineCount(3); // 1 preamble line and 2 actions
    }

    @Test
    void defaultPreambleLineIsRenderedWhenNotSpecificallySet() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .build();

        assertThat(details.action()).startsWith("You can take the following actions to solve the issue:\n");
    }

    @Test
    void preambleLineCanBeSetManually() {
        final var details = DiagnosisDetails.withDescription("description")
            .andSuggestedActions("Do the following:")
            .of("first")
            .build();

        assertThat(details.action()).startsWith("Do the following:\n");
    }

    @Test
    void buildCanBeOmittedWhenUsingAndForTheLastAction() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .of(() -> Optional.of("second"))
            .and("third");

        assertThat(details.action())
            .matches("(?s).*\\bfirst\\b.*")
            .matches("(?s).*\\bsecond\\b.*")
            .matches("(?s).*\\bthird\\b.*");
    }

    @Test
    void buildCanBeOmittedWhenUsingAndWithConditionalAvailableActionForTheLastAction() {
        final var details = DiagnosisDetails.withDescription("description").andSuggestedActions()
            .of("first")
            .of(() -> Optional.of("second"))
            .and(() -> Optional.of("third"));

        assertThat(details.action())
            .matches("(?s).*\\bfirst\\b.*")
            .matches("(?s).*\\bsecond\\b.*")
            .matches("(?s).*\\bthird\\b.*");
    }
}