package de.gcoding.boot.database.auditing;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

import static de.gcoding.boot.database.auditing.FixedNameAuditorAware.DEFAULT_SYSTEM_AUDITOR;

/**
 * Uses springs {@link SecurityContextHolder} in order to get the principal present in the current security context.
 * As an auditor value, the principals name is used by calling {@link Authentication#getName()}. When no principal
 * is present in the security context, this implementation will return the default auditor instead of an empty optional.
 * If not customized through the constructor, the default auditor is {@code 00000000-0000-0000-0000-000000000000}
 */
public class PrincipalNameAuditorAware implements AuditorAware<String> {

    private final String systemAuditor;

    /**
     * Creates a new {@link PrincipalNameAuditorAware} using {@code 00000000-0000-0000-0000-000000000000} for the
     * default auditor in case no principal is present in the security context
     */
    public PrincipalNameAuditorAware() {
        this(DEFAULT_SYSTEM_AUDITOR);
    }

    /**
     * Creates a new {@link PrincipalNameAuditorAware} using the given {@code systemAuditor} for the
     * default auditor in case no principal is present in the security context
     *
     * @param systemAuditor The value for the default auditor that will be returned in case there is no
     *                      principal within the current security context
     */
    public PrincipalNameAuditorAware(@NonNull String systemAuditor) {
        this.systemAuditor = Objects.requireNonNull(systemAuditor, "systemAuditor must not be null");

    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will return the name of the principal that is extracted from the current {@link SecurityContext}.
     * If no such principal is present, the default auditor value is returned instead of an empty optional. The default
     * auditor value can be set through the constructor. If not set explicitly, it will be {@code 00000000-0000-0000-0000-000000000000}.
     * The returned optional will therefore never be empty
     * </p>
     */
    @Nonnull
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .or(() -> Optional.of(systemAuditor));
    }

    /**
     * Checks whether the given {@code auditor} is the system auditor
     *
     * @param auditor The auditor to check against
     * @return {@code true}, if the auditor is the system auditor (not attached to any user), {@code false}, otherwise
     */
    public boolean isSystemAuditor(String auditor) {
        return systemAuditor.equals(auditor);
    }
}
