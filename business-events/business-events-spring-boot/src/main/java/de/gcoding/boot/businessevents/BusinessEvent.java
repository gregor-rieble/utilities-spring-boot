package de.gcoding.boot.businessevents;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * A business event is a spring application event that carries a domain objects or entities model as payload as
 * well as an action that was applied to that payload (such as CREATE, UPDATE, DELETE, ...).
 * </p>
 * <p>
 * You can emit a business event using the standard spring API ({@link ApplicationEventPublisher} or use the
 * annotation based aspect approach with {@link EmitBusinessEvent} annotations.
 * </p>
 * <p>
 * To create new instances of business events programmatically, it is advised to use the builder entrypoints
 * {@link #withEventData(BusinessEventData)} or {@link #withPayload(Object)}, {@link #fromEvent(BusinessEventDataProvider)}
 * </p>
 */
public class BusinessEvent extends ApplicationEvent implements BusinessEventDataProvider {
    private final BusinessEventData eventData;

    /**
     * Creates a new {@link BusinessEvent}
     *
     * @param source    The source object that emitted the event
     * @param eventData The business event data containing the domain model of the affected object, the action as
     *                  well as metadata.
     */
    public BusinessEvent(@Nonnull Object source, @Nonnull BusinessEventData eventData) {
        super(
            source,
            Clock.fixed(
                eventData.timestamp().toInstant(),
                eventData.timestamp().getZone()
            )
        );
        this.eventData = eventData;
    }

    @Nonnull
    @Override
    public BusinessEventData getEventData() {
        return eventData;
    }

    /**
     * Start building a new {@link BusinessEvent} that will contain the given {@code payload}
     *
     * @param payload The payload that the built business event will carry
     * @return The builder that can be used to continue building the business event instance
     */
    @Nonnull
    public static Builder withPayload(@Nonnull Object payload) {
        return new Builder(payload);
    }

    /**
     * Convenience method simply calling:
     * <pre>
     * withEventData(event.getEventData())
     * </pre>
     * See {@link #withEventData(BusinessEventData)} for details
     *
     * @param event The event from which the builder should be initialized
     * @return The builder that can be used to continue building the business event instance
     * @see #withEventData(BusinessEventData)
     */
    @Nonnull
    public static Builder fromEvent(@Nonnull BusinessEventDataProvider event) {
        return withEventData(event.getEventData());
    }

    /**
     * Start building a new {@link BusinessEvent} that will be initialized with all data copied from the given
     * {@code eventData}. Note that the id and timestamp of the event will be copied as well, so you might want to call
     * {@link  Builder#randomId()} and {@link Builder#timestampNow()}, afterward. Also note that the payload will be
     * copied by reference, so if you directly change the internals of the payload, it will affect the built business
     * event as well as the payload of the event that was passed to this method.
     *
     * @param eventData The event data used to initialize the builder
     * @return The builder that can be used to continue building the business event instance
     */
    @Nonnull
    public static Builder withEventData(@Nonnull BusinessEventData eventData) {
        return new Builder(eventData);
    }

    /**
     * A Builder that builds {@link BusinessEvent}s
     */
    public static final class Builder {
        private BusinessEventData eventData;
        private final Map<String, String> metadata;

        private Builder(@Nonnull Object payload) {
            eventData = new BusinessEventData(payload);
            metadata = new HashMap<>();
        }

        private Builder(@Nonnull BusinessEventData initialEventData) {
            eventData = initialEventData;
            metadata = new HashMap<>(eventData.metadata());
        }

        /**
         * Sets the given payload to be used in the built event
         *
         * @param payload The payload used by the event being built
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder payload(@Nonnull Object payload) {
            eventData = eventData.withPayload(payload);
            return this;
        }

        /**
         * Sets the given timestamp to be used in the built event. If no timestamp is set at the time of
         * finalizing the building, the current date time will be used derived by the system time
         *
         * @param timestamp The timestamp used by the event being built
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder timestamp(@Nonnull ZonedDateTime timestamp) {
            eventData = eventData.withTimestamp(timestamp);
            return this;
        }

        /**
         * <pre>
         * timestamp(ZonedDateTime.now())
         * </pre>
         *
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder timestampNow() {
            return timestamp(ZonedDateTime.now());
        }

        /**
         * Sets the given action for the event being built. If no action is set at the time of
         * finalizing the building or if a blank value was specified, {@link EventActions#NONE} will be used
         * for the action, instead
         *
         * @param action The action for the event being built
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder action(@Nonnull String action) {
            eventData = eventData.withAction(action);
            return this;
        }

        /**
         * Sets the given id for the event being built. If no id is set at the time of finalizing the building,
         * a randomly generated UUID will be used, instead
         *
         * @param id The id for the event being built
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder id(@Nonnull UUID id) {
            eventData = eventData.withId(id);
            return this;
        }

        /**
         * Shortcut for
         * <pre>
         * id(UUID.randomId())
         * </pre>
         *
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder randomId() {
            return id(UUID.randomUUID());
        }

        /**
         * Sets the given metadata for the event being built replacing all existing metadata present in the builder.
         * This method copies the given map, so changes within the passed map are not reflected in the metadata
         * of the builder.
         *
         * @param metadata The metadata for the event being built
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder metadata(@Nonnull Map<String, String> metadata) {
            this.metadata.clear();
            addMetadata(metadata);
            return this;
        }

        /**
         * Adds the given metadata entry to the builders metadata map replacing any existing metadata value with the
         * same key.
         *
         * @param metadataKey The key for the metadata value
         * @param value       The value of the metadata for the specified key
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder addMetadata(@Nonnull String metadataKey, @Nonnull String value) {
            metadata.put(metadataKey, value);
            return this;
        }

        /**
         * Adds the given metadata to the builders metadata map replacing any existing metadata value with the
         * same keys. Because the metadata is copied into the builder, later changes of the passed
         * {@code additionalMetadata} will not be reflected in the metadata of the builder
         *
         * @param additionalMetadata Additional metadata to be added to the builder
         * @return The builder that can be used to continue building the business event instance
         */
        @Nonnull
        public Builder addMetadata(@Nonnull Map<String, String> additionalMetadata) {
            metadata.putAll(additionalMetadata);
            return this;
        }

        /**
         * Builds &amp; Creates the {@link BusinessEvent} instance according to the builders state. Will use this builder
         * as {@code source} property for the newly built event.
         *
         * @return A new business event instance
         */
        @Nonnull
        public BusinessEvent build() {
            return build(this);
        }

        /**
         * Builds &amp; Creates the {@link BusinessEvent} instance according to the builders state. Will use the given
         * {@code source} as source property for the newly built event.
         *
         * @return A new business event instance with {@code source} as its source
         */
        @Nonnull
        public BusinessEvent build(@Nonnull Object source) {
            final var data = eventData.withMetadata(metadata);

            return new BusinessEvent(source, data);
        }
    }
}
