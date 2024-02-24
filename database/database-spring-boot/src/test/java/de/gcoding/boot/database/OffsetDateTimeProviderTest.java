package de.gcoding.boot.database;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetDateTimeProviderTest {
    static final ZoneId STATIC_ZONE_ID = ZoneId.of("UTC");
    static final Instant STATIC_CLOCK_TIME = Instant.ofEpochMilli(637196400000L);
    static final Clock STATIC_CLOCK = Clock.fixed(STATIC_CLOCK_TIME, STATIC_ZONE_ID);
    OffsetDateTimeProvider offsetDateTimeProvider = new OffsetDateTimeProvider();

    @Test
    void whenTimestampIsRequestedResultIsOfTypeOffsetDateTime() {
        final var result = offsetDateTimeProvider.getNow();

        assertThat(result)
            .isNotEmpty()
            .hasValueSatisfying(time -> assertThat(time).isInstanceOf(OffsetDateTime.class));
    }

    @Test
    void whenTimestampIsRequestedSystemTimeIsUsedAsSource() {
        final var before = OffsetDateTime.now();
        final var result = (OffsetDateTime) offsetDateTimeProvider.getNow().orElseThrow();
        final var after = OffsetDateTime.now();

        assertThat(result).isBetween(before, after);
    }

    @Test
    void whenClockIsSpecifiedItIsUsedToCalculateTimestamps() {
        offsetDateTimeProvider = new OffsetDateTimeProvider(STATIC_CLOCK);

        final var result = (OffsetDateTime) offsetDateTimeProvider.getNow().orElseThrow();

        assertThat(result.toInstant()).isEqualTo(STATIC_CLOCK_TIME);
    }
}