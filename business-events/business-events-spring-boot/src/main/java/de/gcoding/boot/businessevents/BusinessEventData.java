package de.gcoding.boot.businessevents;


import jakarta.annotation.Nonnull;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * Creates a business event data record that holds data for a business event
 *
 * @param id        The UUID of the event
 * @param payload   The payload of the event. Typically, an entity or domain model
 * @param action    The action that was applied to the payload
 * @param timestamp The timestamp when the event occurred
 * @param metadata  Additional metadata describing the event
 */
public record BusinessEventData(
    @Nonnull UUID id,
    @Nonnull Object payload,
    @Nonnull String action,
    @Nonnull ZonedDateTime timestamp,
    @Nonnull Map<String, String> metadata
) {
    public BusinessEventData {
        requireNonNull(id, "id must not be null");
        requireNonNull(payload, "payload must not be null");
        requireNonNull(action, "action must not be null");
        requireNonNull(timestamp, "timestamp must not be null");
        requireNonNull(metadata, "metadata must not be null");

        metadata = Map.copyOf(metadata);
    }

    /**
     * Creates a business event data record that holds data for a business event. The resulting record will have
     * the following defaults:
     * <ul>
     *     <li><strong>id</strong>: New auto generated UUID</li>
     *     <li><strong>action</strong>: {@code NONE}</li>
     *     <li><strong>timestamp</strong>: A generated timestamp using the current system time</li>
     *     <li><strong>metadata</strong>: An empty map</li>
     * </ul>
     *
     * @param payload The payload of the event. Typically, an entity or domain model
     */
    public BusinessEventData(@Nonnull Object payload) {
        this(payload, EventActions.NONE);
    }

    /**
     * Creates a business event data record that holds data for a business event. The resulting record will have
     * the following defaults:
     * <ul>
     *     <li><strong>id</strong>: New auto generated UUID</li>
     *     <li><strong>timestamp</strong>: A generated timestamp using the current system time</li>
     *     <li><strong>metadata</strong>: An empty map</li>
     * </ul>
     *
     * @param payload The payload of the event. Typically, an entity or domain model
     * @param action  The action that was applied to the payload
     */
    public BusinessEventData(@Nonnull Object payload, @Nonnull String action) {
        this(UUID.randomUUID(), payload, action);
    }

    /**
     * Creates a business event data record that holds data for a business event. The resulting record will have
     * the following defaults:
     * <ul>
     *     <li><strong>timestamp</strong>: A generated timestamp using the current system time</li>
     *     <li><strong>metadata</strong>: An empty map</li>
     * </ul>
     *
     * @param id      The UUID of the event
     * @param payload The payload of the event. Typically, an entity or domain model
     * @param action  The action that was applied to the payload
     */
    public BusinessEventData(@Nonnull UUID id, @Nonnull Object payload, @Nonnull String action) {
        this(id, payload, action, ZonedDateTime.now());
    }

    /**
     * Creates a business event data record that holds data for a business event. The resulting record will have
     * the following defaults:
     * <ul>
     *     <li><strong>timestamp</strong>: A generated timestamp using the current system time</li>
     * </ul>
     *
     * @param id       The UUID of the event
     * @param payload  The payload of the event. Typically, an entity or domain model
     * @param action   The action that was applied to the payload
     * @param metadata Additional metadata describing the event
     */
    public BusinessEventData(@Nonnull UUID id, @Nonnull Object payload, @Nonnull String action, @Nonnull Map<String, String> metadata) {
        this(id, payload, action, ZonedDateTime.now(), metadata);
    }


    /**
     * Creates a business event data record that holds data for a business event. The resulting record will have
     * the following defaults:
     * <ul>
     *     <li><strong>metadata</strong>: An empty map</li>
     * </ul>
     *
     * @param id        The UUID of the event
     * @param payload   The payload of the event. Typically, an entity or domain model
     * @param action    The action that was applied to the payload
     * @param timestamp The timestamp when the event occurred
     */
    public BusinessEventData(@Nonnull UUID id, @Nonnull Object payload, @Nonnull String action, @Nonnull ZonedDateTime timestamp) {
        this(id, payload, action, timestamp, Map.of());
    }

    /**
     * Copy constructor for event data, will copy all properties including the id and timestamp. Note that the metadata
     * map of the source instance will be copied to a new map, so that changes in one of the instance will not affect
     * the other.
     *
     * @param eventData The event data from which to copy attributes
     */
    public BusinessEventData(@Nonnull BusinessEventData eventData) {
        this(eventData.id, eventData.payload, eventData.action, eventData.timestamp, eventData.metadata);
    }

    /**
     * Returns a new business event data object that has all the properties of the current instance but with the
     * given id applied to it.
     *
     * @param id The id to be used in the new business event data
     * @return A new business event data object with the given id
     */
    @Nonnull
    public BusinessEventData withId(@Nonnull UUID id) {
        return new BusinessEventData(id, payload, action, timestamp, metadata);
    }

    /**
     * Returns a new business event data object that has all the properties of the current instance but with the
     * given payload applied to it.
     *
     * @param payload The payload to be used in the new business event data
     * @return A new business event data object with the given id
     */
    @Nonnull
    public BusinessEventData withPayload(@Nonnull Object payload) {
        return new BusinessEventData(id, payload, action, timestamp, metadata);
    }

    /**
     * Returns a new business event data object that has all the properties of the current instance but with the
     * given action applied to it.
     *
     * @param action The action to be used in the new business event data
     * @return A new business event data object with the given id
     */
    @Nonnull
    public BusinessEventData withAction(@Nonnull String action) {
        return new BusinessEventData(id, payload, action, timestamp, metadata);
    }

    /**
     * Returns a new business event data object that has all the properties of the current instance but with the
     * given timestamp applied to it.
     *
     * @param timestamp The timestamp to be used in the new business event data
     * @return A new business event data object with the given id
     */
    @Nonnull
    public BusinessEventData withTimestamp(@Nonnull ZonedDateTime timestamp) {
        return new BusinessEventData(id, payload, action, timestamp, metadata);
    }

    /**
     * Returns a new business event data object that has all the properties of the current instance but with the
     * given metadata applied to it. The passed metadata will be copied into a new unmodifiable map so that later
     * changes of the original map will not affect the metadata within the record
     *
     * @param metadata The metadata to be used in the new business event data
     * @return A new business event data object with the given id
     */
    @Nonnull
    public BusinessEventData withMetadata(@Nonnull Map<String, String> metadata) {
        return new BusinessEventData(id, payload, action, timestamp, metadata);
    }
}