package de.gcoding.boot.database.auditing;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FixedNameAuditorAware implements AuditorAware<String> {
    public static final String DEFAULT_SYSTEM_AUDITOR = new UUID(0L, 0L).toString();

    private final String auditor;

    public FixedNameAuditorAware() {
        this(DEFAULT_SYSTEM_AUDITOR);
    }

    public FixedNameAuditorAware(@NonNull String auditor) {
        this.auditor = Objects.requireNonNull(auditor, "auditor must not be null");
    }

    @Nonnull
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(auditor);
    }
}
