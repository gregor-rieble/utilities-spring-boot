package de.gcoding.boot.database.auditing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.gcoding.boot.database.auditing.FixedNameAuditorAware.DEFAULT_SYSTEM_AUDITOR;
import static org.assertj.core.api.Assertions.assertThat;

class FixedNameAuditorAwareTest {
    FixedNameAuditorAware fixedNameAuditorAware = new FixedNameAuditorAware();

    @Test
    void auditorReturnsSystemAuditorByDefault() {
        final var auditor = fixedNameAuditorAware.getCurrentAuditor();

        assertThat(auditor).hasValue(DEFAULT_SYSTEM_AUDITOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"auditor", "admin", "manager"})
    void auditorReturnsSpecifiedAuditor(String fixedAuditor) {
        fixedNameAuditorAware = new FixedNameAuditorAware(fixedAuditor);

        final var auditor = fixedNameAuditorAware.getCurrentAuditor();

        assertThat(auditor).hasValue(fixedAuditor);
    }
}