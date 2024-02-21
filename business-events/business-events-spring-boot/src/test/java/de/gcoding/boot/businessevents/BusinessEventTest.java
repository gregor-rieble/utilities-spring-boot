package de.gcoding.boot.businessevents;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessEventTest {
    static final String STRING_PAYLOAD = "string";
    static final Integer INTEGER_PAYLOAD = 1337;
    static final Object OBJECT_PAYLOAD = new Object();
    static final UUID EVENT_ID = UUID.fromString("7ba7aa8a-0476-41f8-93a4-7a7067901cdc");
    static final ZonedDateTime EVENT_TIMESTAMP = ZonedDateTime.parse("2007-12-03T10:15:30+01:00");
    static final Set<UUID> generatedIds = new HashSet<>();

    @Test
    void whenEventIsCreatedAPayloadIsPresent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        assertThat(event.getPayload()).isEqualTo(STRING_PAYLOAD);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenNullIsGivenDuringCreationAnExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> BusinessEvent.withPayload(null).build());
    }

    @Test
    void whenGetPayloadClassIsPassedValueIsAutoCasted() {
        final var stringPayload = BusinessEvent.withPayload(STRING_PAYLOAD).build()
            .getPayloadAs(String.class);
        assertThat(stringPayload).isInstanceOf(String.class);

        final var integerPayload = BusinessEvent.withPayload(INTEGER_PAYLOAD).build()
            .getPayloadAs(Integer.class);
        assertThat(integerPayload).isInstanceOf(Integer.class);

        final var objectPayload = BusinessEvent.withPayload(OBJECT_PAYLOAD).build()
            .getPayloadAs(Object.class);
        assertThat(objectPayload).isInstanceOf(Object.class);
    }

    @Test
    void whenPayloadIsReplacedEventWillHaveNewValue() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .payload(INTEGER_PAYLOAD)
            .build();
        assertThat(event.getPayload()).isEqualTo(INTEGER_PAYLOAD);
    }

    @RepeatedTest(20)
    void whenNoIdIsGivenARandomUUIDIsAssigned(RepetitionInfo repetitionInfo) {
        final var repetition = repetitionInfo.getCurrentRepetition();
        if (repetition == 1) {
            generatedIds.clear();
        }

        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        assertThat(event.getId()).isNotNull().isNotIn(generatedIds);
        generatedIds.add(event.getId());
        assertThat(generatedIds).hasSize(repetition);
    }

    @Test
    void whenIdIsAssignedExplicitlyItIsSetInEvent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).id(EVENT_ID).build();
        assertThat(event.getId()).isEqualTo(EVENT_ID);
    }

    @RepeatedTest(20)
    void whenIdIsRandomlyGeneratedARandomUUIDIsSetInEvent(RepetitionInfo repetitionInfo) {
        final var repetition = repetitionInfo.getCurrentRepetition();
        if (repetition == 1) {
            generatedIds.clear();
        }

        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).randomId().build();
        assertThat(event.getId()).isNotNull().isNotIn(generatedIds);
        generatedIds.add(event.getId());
        assertThat(generatedIds).hasSize(repetition);
    }

    @Test
    void whenNoTimestampIsGivenCurrentTimeIsUsed() {
        final var beforeTest = ZonedDateTime.now();
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        final var afterTest = ZonedDateTime.now();
        assertThat(event.getEventDataTimestamp()).isBetween(beforeTest, afterTest);
    }

    @Test
    void whenEventDataTimestampExistsSpringTimestampIsTheSame() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        assertThat(event.getTimestamp()).isEqualTo(event.getEventDataTimestamp().toInstant().toEpochMilli());
    }

    @Test
    void whenTimestampIsAssignedExplicitlyItIsSetInEvent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).timestamp(EVENT_TIMESTAMP).build();
        assertThat(event.getEventDataTimestamp()).isEqualTo(EVENT_TIMESTAMP);
    }

    @Test
    void whenTimestampNowIsCalledATimestampDerivedFromTheSystemTimeIsSetInEvent() {
        final var timeBefore = ZonedDateTime.now();
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).timestampNow().build();
        final var timeAfter = ZonedDateTime.now();

        assertThat(event.getEventDataTimestamp()).isBetween(timeBefore, timeAfter);
    }

    @Test
    void whenNoActionIsGivenNoneIsUsed() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        assertThat(event.getAction()).isEqualTo(EventActions.NONE);
    }

    @ParameterizedTest
    @ValueSource(strings = {EventActions.NONE, EventActions.CREATE, EventActions.DELETE, EventActions.UPDATE, "custom"})
    void whenTimestampIsAssignedExplicitlyItIsSetInEvent(String action) {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).action(action).build();
        assertThat(event.getAction()).isEqualTo(action);
    }

    @Test
    void whenNoMetadataIsPassedMetadataIsEmptyButNotNull() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD).build();
        assertThat(event.getMetadata()).isNotNull().isEmpty();
    }

    @Test
    void whenMetadataIsPassedItIsSetInEvent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(Map.of("existing", "metadata"))
            .build();

        assertThat(event.getMetadata()).isEqualTo(Map.of("existing", "metadata"));
    }

    @Test
    void whenMetadataIsPassedExistingMetadataIsOverridden() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(Map.of("existing", "metadata"))
            .metadata(Map.of("new", "metadata"))
            .build();

        assertThat(event.getMetadata())
            .hasSize(1)
            .containsEntry("new", "metadata")
            .doesNotContainKey("existing");
    }

    @Test
    void whenOriginalMetadataMapIsAdaptedItWillNotReflectTheMetadataAfterEventHasBeenBuilt() {
        final var originalMetadata = new HashMap<String, String>();
        originalMetadata.put("existing", "metadata");

        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(originalMetadata)
            .build();

        originalMetadata.put("additional", "metadata");

        assertThat(event.getMetadata())
            .hasSize(1)
            .containsEntry("existing", "metadata")
            .doesNotContainKey("additional");
    }

    @Test
    void whenMetadataIsAddedExistingMetadataIsStillPresent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(new HashMap<>(Map.of("existing", "metadata")))
            .addMetadata("additional", "value")
            .addMetadata("third", "data")
            .build();

        assertThat(event.getMetadata())
            .hasSize(3)
            .containsEntry("existing", "metadata")
            .containsEntry("additional", "value")
            .containsEntry("third", "data");
    }

    @Test
    void whenMetadataMapIsAddedExistingMetadataIsStillPresent() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(new HashMap<>(Map.of("existing", "metadata")))
            .addMetadata(Map.of("additional", "value", "third", "data"))
            .build();

        assertThat(event.getMetadata())
            .hasSize(3)
            .containsEntry("existing", "metadata")
            .containsEntry("additional", "value")
            .containsEntry("third", "data");
    }

    @Test
    void whenMetadataIsPresentSingleValuesCanBeRetrieved() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(Map.of(
                "existing", "metadata",
                "additional", "value",
                "third", "data"
            ))
            .build();

        assertThat(event.getMetadata("existing")).hasValue("metadata");
        assertThat(event.getMetadata("additional")).hasValue("value");
        assertThat(event.getMetadata("third")).hasValue("data");
    }

    @Test
    void whenMetadataIsPresentRetrievingNonExistentSingleValueReturnsEmptyOptional() {
        final var event = BusinessEvent.withPayload(STRING_PAYLOAD)
            .metadata(Map.of(
                "existing", "metadata",
                "additional", "value",
                "third", "data"
            ))
            .build();

        assertThat(event.getMetadata("non-existing")).isEmpty();
    }

    @Test
    void whenEventIsBuildFromExistingEventAllAttributesAreCopied() {
        final var original = BusinessEvent.withPayload(STRING_PAYLOAD)
            .timestamp(EVENT_TIMESTAMP)
            .action(EventActions.UPDATE)
            .id(EVENT_ID)
            .metadata(Map.of("existing", "metadata"))
            .build();

        final var copied = BusinessEvent.fromEvent(original).build();

        assertThat(copied.getTimestamp()).isEqualTo(original.getTimestamp());

        assertThat(copied.getEventData()).isNotSameAs(original.getEventData()).isEqualTo(original.getEventData());
        assertThat(copied.getId()).isEqualTo(original.getId());
        assertThat(copied.getEventDataTimestamp()).isEqualTo(original.getEventDataTimestamp());
        assertThat(copied.getAction()).isEqualTo(original.getAction());
        assertThat(copied.getMetadata()).isEqualTo(original.getMetadata());
        assertThat(copied.getPayload()).isEqualTo(original.getPayload());
    }
}