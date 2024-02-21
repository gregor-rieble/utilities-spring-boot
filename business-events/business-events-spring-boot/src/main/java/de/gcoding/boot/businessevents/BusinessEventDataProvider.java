package de.gcoding.boot.businessevents;


import jakarta.annotation.Nonnull;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides {@link BusinessEventData}
 */
public interface BusinessEventDataProvider {
    /**
     * Returns the {@link BusinessEventData} that belongs to this instance
     *
     * @return The business event data
     */
    @Nonnull
    BusinessEventData getEventData();

    /**
     * Returns the id of the event
     *
     * @return The id of the event
     */
    @Nonnull
    default UUID getId() {
        return getEventData().id();
    }

    /**
     * Returns the timestamp of the event
     *
     * @return The timestamp of the event
     */
    @Nonnull
    default ZonedDateTime getEventDataTimestamp() {
        return getEventData().timestamp();
    }

    /**
     * Returns the action that was applied to the events payload
     *
     * @return The action that was applied to the events payload
     */
    @Nonnull
    default String getAction() {
        return getEventData().action();
    }

    /**
     * Returns the metadata that are attached to the event
     *
     * @return The metadata that are attached to the event
     */
    @Nonnull
    default Map<String, String> getMetadata() {
        return getEventData().metadata();
    }

    /**
     * Returns a single metadata entry with the specified {@code metadataKey}
     *
     * @param metadataKey The key of the metadata entry that should be retrieved
     * @return The optional containing the value belonging to the given key or an empty optional if no such entry exists
     */
    @Nonnull
    default Optional<String> getMetadata(String metadataKey) {
        return Optional.ofNullable(getMetadata().get(metadataKey));
    }

    /**
     * Returns the payload of the event
     *
     * @return The payload of the event
     */
    @Nonnull
    default Object getPayload() {
        return getEventData().payload();
    }

    /**
     * Returns the payload of the event as the desired type. Note that the caller must ensure that casting the payload
     * to the desired type is possible, otherwise a {@link ClassCastException} is thrown
     *
     * @param type The desired type of the payload
     * @return The payload of the event in the desired type
     * @throws ClassCastException If the payload cannot be converted to the desired type
     */
    @Nonnull
    @SuppressWarnings({"unchecked", "unused"})
    default <T> T getPayloadAs(Class<T> type) {
        return (T) getEventData().payload();
    }
}
