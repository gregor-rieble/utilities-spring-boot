package de.gcoding.boot.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PrincipalIdAuditorAwareTest {
    PrincipalIdAuditorAware principalIdAuditorAware = new PrincipalIdAuditorAware();

    @Test
    void defaultSystemAuditorIsZeroUUID() {
        final var auditor = principalIdAuditorAware.getCurrentAuditor();

        assertThat(auditor)
            .isPresent()
            .hasValue("00000000-0000-0000-0000-000000000000");
    }

    @Test
    void whenNoAuditorIsPresentReturnSystemAuditor() {
        final var auditor = principalIdAuditorAware.getCurrentAuditor();

        assertThat(auditor)
            .map(principalIdAuditorAware::isSystemAuditor)
            .isPresent()
            .hasValue(true);
    }

    @Test
    void whenAuditorIsPresentIsSystemAuditorReturnsFalse() {
        withPrincipal("user-principal", () -> {
            final var auditor = principalIdAuditorAware.getCurrentAuditor();

            assertThat(auditor)
                .map(principalIdAuditorAware::isSystemAuditor)
                .isPresent()
                .hasValue(false);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void principalNameIsUsedWhenSecurityContextIsPresent(String principal) {
        withPrincipal(principal, () -> {
            final var auditor = principalIdAuditorAware.getCurrentAuditor();

            assertThat(auditor).hasValue(principal);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void useCustomDefaultSystemPrincipal(String principal) {
        principalIdAuditorAware = new PrincipalIdAuditorAware(principal);
        final var auditor = principalIdAuditorAware.getCurrentAuditor();

        assertThat(auditor)
            .hasValue(principal)
            .map(principalIdAuditorAware::isSystemAuditor)
            .isPresent()
            .hasValue(true);
    }

    private void withPrincipal(String principalName, Runnable runnable) {
        final var previous = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principalName, "pw", Set.of()));
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previous);
        }
    }
}