package de.gcoding.boot.diagnostics;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

@ExtendWith(OutputCaptureExtension.class)
class DiagnosableFailureAnalyzerIsTriggeredIT {
    @Test
    void failureAnalyzerIsTriggered(CapturedOutput output) {
        assertThatException().isThrownBy(() -> new SpringApplicationBuilder(TestConfiguration.class).run());

        assertThat(output)
            .contains("APPLICATION FAILED TO START")
            .contains("Analyzed Failure Description")
            .contains("Suggestion To Fix Analyzed Failure");
    }

    @Configuration
    static class TestConfiguration {
        @PostConstruct
        void fail() {
            throw new DiagnosableException("Analyzed Failure Description", "Suggestion To Fix Analyzed Failure");
        }
    }
}
