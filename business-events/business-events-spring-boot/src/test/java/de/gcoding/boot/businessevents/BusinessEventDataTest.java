package de.gcoding.boot.businessevents;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventDataTest {
    static final UUID ID = UUID.fromString("97fa4d2a-51ec-4e11-915c-b5beeba4c75b");
    static final Object PAYLOAD = new Object();
    static final String ACTION = EventActions.CREATE;
    static final ZonedDateTime TIMESTAMP = ZonedDateTime.now();
    static final Map<String, String> METADATA = new HashMap<>(Map.of("key", "value"));
    final static Set<UUID> GENERATED_IDS = new HashSet<>();

    @Test
    void whenPayloadIsPassedSameInstanceIsReturnedInGetter() {
        final var payloads = new HashSet<>();

        payloads.add(new BusinessEventData(PAYLOAD).payload());
        payloads.add(new BusinessEventData(PAYLOAD, ACTION).payload());
        payloads.add(new BusinessEventData(ID, PAYLOAD, ACTION).payload());
        payloads.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP).payload());
        payloads.add(new BusinessEventData(ID, PAYLOAD, ACTION, METADATA).payload());
        payloads.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA).payload());

        assertThat(payloads)
            .hasSize(1)
            .allMatch(p -> p == PAYLOAD);
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.NONE, EventActions.CREATE, EventActions.UPDATE, EventActions.DELETE})
    void whenActionIsPassedItIsReturnedInGetter(String action) {
        final var actions = new HashSet<String>();

        actions.add(new BusinessEventData(PAYLOAD, action).action());
        actions.add(new BusinessEventData(ID, PAYLOAD, action).action());
        actions.add(new BusinessEventData(ID, PAYLOAD, action, TIMESTAMP).action());
        actions.add(new BusinessEventData(ID, PAYLOAD, action, METADATA).action());
        actions.add(new BusinessEventData(ID, PAYLOAD, action, TIMESTAMP, METADATA).action());

        assertThat(actions)
            .hasSize(1)
            .allMatch(action::equals);
    }

    @Test
    void whenIdIsPassedItIsReturnedInGetter() {
        final var ids = new HashSet<UUID>();

        ids.add(new BusinessEventData(ID, PAYLOAD, ACTION).id());
        ids.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP).id());
        ids.add(new BusinessEventData(ID, PAYLOAD, ACTION, METADATA).id());
        ids.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA).id());

        assertThat(ids)
            .hasSize(1)
            .allMatch(ID::equals);
    }

    @Test
    void whenTimestampIsPassedSameInstanceIsReturnedInGetter() {
        final var timestamps = new HashSet<ZonedDateTime>();

        timestamps.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP).timestamp());
        timestamps.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA).timestamp());

        assertThat(timestamps)
            .hasSize(1)
            .allMatch(ts -> ts == TIMESTAMP);
    }

    @Test
    void whenMetadataIsPassedGetterReturnsAnEqualMetadataMap() {
        final var metadata = new LinkedList<Map<String, String>>();

        metadata.add(new BusinessEventData(ID, PAYLOAD, ACTION, METADATA).metadata());
        metadata.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA).metadata());

        assertThat(metadata)
            .hasSize(2)
            .allMatch(METADATA::equals);
    }

    @Test
    void whenMetadataIsPassedGetterReturnsCopiedInstance() {
        final var metadata = new LinkedList<Map<String, String>>();

        metadata.add(new BusinessEventData(ID, PAYLOAD, ACTION, METADATA).metadata());
        metadata.add(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA).metadata());

        assertThat(metadata)
            .hasSize(2)
            .allMatch(m -> m != METADATA);
    }

    @Test
    void whenMetadataIsRetrievedItIsUnmodifiable() {
        final var eventData = new BusinessEventData(ID, PAYLOAD, ACTION, METADATA);

        final var metadata = eventData.metadata();

        org.junit.jupiter.api.Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> metadata.put("test", "value")
        );

        assertThat(metadata).isEqualTo(METADATA);
    }

    @Test
    void whenCopyConstructorIsUsedAllValuesAreCopied() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var copied = new BusinessEventData(initial);

        assertThat(copied).isEqualTo(initial);
    }

    @Test
    void whenWithIdIsUsedANewInstanceIsReturnedWithTheDesiredValue() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var newId = UUID.randomUUID();
        final var adapted = initial.withId(newId);

        assertThat(adapted.id()).isEqualTo(newId);
        assertThat(adapted).isNotSameAs(initial).isEqualTo(new BusinessEventData(newId, PAYLOAD, ACTION, TIMESTAMP, METADATA));
    }

    @Test
    void whenWithPayloadIsUsedANewInstanceIsReturnedWithTheDesiredValue() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var newPayload = "string payload";
        final var adapted = initial.withPayload(newPayload);

        assertThat(adapted.payload()).isEqualTo(newPayload);
        assertThat(adapted).isNotSameAs(initial).isEqualTo(new BusinessEventData(ID, newPayload, ACTION, TIMESTAMP, METADATA));
    }

    @Test
    void whenWithActionIsUsedANewInstanceIsReturnedWithTheDesiredValue() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var newAction = "custom";
        final var adapted = initial.withAction(newAction);

        assertThat(adapted.action()).isEqualTo(newAction);
        assertThat(adapted).isNotSameAs(initial).isEqualTo(new BusinessEventData(ID, PAYLOAD, newAction, TIMESTAMP, METADATA));
    }

    @Test
    void whenWithTimestampIsUsedANewInstanceIsReturnedWithTheDesiredValue() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var newTimestamp = ZonedDateTime.now();
        final var adapted = initial.withTimestamp(newTimestamp);

        assertThat(adapted.timestamp()).isEqualTo(newTimestamp);
        assertThat(adapted).isNotSameAs(initial).isEqualTo(new BusinessEventData(ID, PAYLOAD, ACTION, newTimestamp, METADATA));
    }

    @Test
    void whenWithMetadataIsUsedANewInstanceIsReturnedWithTheDesiredValue() {
        final var initial = new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, METADATA);

        final var newMetadata = Map.of("different", "metadata");
        final var adapted = initial.withMetadata(newMetadata);

        assertThat(adapted.metadata()).isEqualTo(newMetadata);
        assertThat(adapted).isNotSameAs(initial).isEqualTo(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, newMetadata));
    }

    @RepeatedTest(10)
    void whenNoIdIsPassedOneIsGenerated(RepetitionInfo repetitionInfo) {
        GENERATED_IDS.add(new BusinessEventData(PAYLOAD).id());
        GENERATED_IDS.add(new BusinessEventData(PAYLOAD, ACTION).id());

        assertThat(GENERATED_IDS)
            .hasSize(repetitionInfo.getCurrentRepetition() * 2)
            .allMatch(Objects::nonNull);
    }

    @Test
    void whenNoActionIsPassedNoneIsUsedAsDefault() {
        final var eventData = new BusinessEventData(PAYLOAD);
        assertThat(eventData.action()).isEqualTo(EventActions.NONE);
    }

    @Test
    void whenNoTimestampIsPassedCurrentTimeIsUsed() {
        final var generatedTimestamps = new HashSet<ZonedDateTime>();
        final var before = ZonedDateTime.now();

        generatedTimestamps.add(new BusinessEventData(PAYLOAD).timestamp());
        generatedTimestamps.add(new BusinessEventData(PAYLOAD, ACTION).timestamp());
        generatedTimestamps.add(new BusinessEventData(ID, PAYLOAD, ACTION).timestamp());
        generatedTimestamps.add(new BusinessEventData(ID, PAYLOAD, ACTION, METADATA).timestamp());

        final var after = ZonedDateTime.now();

        assertThat(generatedTimestamps)
            .isNotEmpty()
            .allSatisfy(timestamp -> assertThat(timestamp).isBetween(before, after));
    }

    @Test
    void whenNoMetadataIsPassedEmptyMetadataMapIsUsed() {
        final var metadata = new HashMap<String, String>();

        metadata.putAll(new BusinessEventData(PAYLOAD).metadata());
        metadata.putAll(new BusinessEventData(PAYLOAD, ACTION).metadata());
        metadata.putAll(new BusinessEventData(ID, PAYLOAD, ACTION).metadata());
        metadata.putAll(new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP).metadata());

        assertThat(metadata).isEmpty();
    }

    @Test
    void whenNullPayloadIsPassedAnExceptionIsThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null, ACTION));
    }

    @Test
    void whenNullIdIsPassedAnExceptionIsThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null, PAYLOAD, ACTION));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null, PAYLOAD, ACTION, TIMESTAMP));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null, PAYLOAD, ACTION, METADATA));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(null, PAYLOAD, ACTION, TIMESTAMP, METADATA));
    }

    @Test
    void whenNullActionIsPassedAnExceptionIsThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(PAYLOAD, null));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, null));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, null, TIMESTAMP));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, null, METADATA));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, null, TIMESTAMP, METADATA));
    }

    @Test
    void whenNullTimestampIsPassedAnExceptionIsThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, ACTION, (ZonedDateTime) null));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, ACTION, null, METADATA));
    }

    @Test
    void whenNullMetadataIsPassedAnExceptionIsThrown() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, ACTION, (Map<String, String>) null));
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> new BusinessEventData(ID, PAYLOAD, ACTION, TIMESTAMP, null));
    }
}