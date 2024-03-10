package de.gcoding.boot.database.auditing;

import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.lang.NonNull;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.Optional;

/**
 * Prices an instance of {@link OffsetDateTime} as the current time. Never produces an empty optional
 */
public class OffsetDateTimeProvider implements DateTimeProvider {
    private final Clock clock;

    /**
     * Creates a new {@link OffsetDateTimeProvider} using the current system time in order to create
     * new {@link OffsetDateTime} instances
     */
    public OffsetDateTimeProvider() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a new {@link OffsetDateTimeProvider} using the given clock in order to create
     * new {@link OffsetDateTime} instances
     */
    public OffsetDateTimeProvider(@NonNull Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The returned optional will never be empty and will contain an instance of {@link OffsetDateTime}
     * </p>
     */
    @Override
    @NonNull
    public Optional<TemporalAccessor> getNow() {
        final var zoneId = clock.getZone();
        final var instant = clock.instant();

        final var offsetDateTime = OffsetDateTime.ofInstant(instant, zoneId);
        return Optional.of(offsetDateTime);
    }
}
