package de.gcoding.boot.businessevents.test;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.BusinessEventDataProvider;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowingConsumer;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BusinessEventRecorder implements ApplicationListener<BusinessEvent> {
    private final List<BusinessEventDataProvider> recordedEvents = Collections.synchronizedList(new ArrayList<>());

    private final List<Consumer<BusinessEventDataProvider>> businessEventListeners = new LinkedList<>();

    @Override
    public void onApplicationEvent(@NonNull BusinessEvent event) {
        recordedEvents.add(event);
        businessEventListeners.forEach(listener -> listener.accept(event));
    }

    public BusinessEventRecorder addBusinessEventListener(Consumer<BusinessEventDataProvider> businessEventListener) {
        businessEventListeners.add(businessEventListener);
        return this;
    }

    public List<BusinessEventDataProvider> getRecordedEvents() {
        return List.copyOf(recordedEvents);
    }

    public void reset() {
        recordedEvents.clear();
        businessEventListeners.clear();
    }

    public int getNumEmitted() {
        return recordedEvents.size();
    }

    public BusinessEventsAssertions<Object> assertThat() {
        return new BusinessEventsAssertions<>(getRecordedEvents());
    }

    public <T> BusinessEventsAssertions<T> assertThatAllEventsWithPayloadType(Class<T> payloadType) {
        return assertThat().fromEventsWithPayloadType(payloadType);
    }

    public static class BusinessEventsAssertions<T> {
        private final BusinessEventsAssertions<Object> root;
        private final List<BusinessEventDataProvider> recordedEvents;

        public BusinessEventsAssertions(List<BusinessEventDataProvider> recordedEvents) {
            this(null, recordedEvents);
        }

        public BusinessEventsAssertions(BusinessEventsAssertions<Object> root, List<BusinessEventDataProvider> recordedEvents) {
            this.root = root;
            this.recordedEvents = recordedEvents;
        }

        @SuppressWarnings("unchecked")
        public <S> BusinessEventsAssertions<S> fromEventsWithPayloadType(Class<S> type) {
            return (BusinessEventsAssertions<S>) fromEventsMatching(event -> event.getPayload().getClass() == type);
        }

        public BusinessEventsAssertions<T> fromEventsWithAction(String action) {
            return fromEventsWithOneOfTheActions(action);
        }

        public BusinessEventsAssertions<T> fromEventsWithOneOfTheActions(String... actions) {
            return fromEventsMatching(event -> Arrays.asList(actions).contains(event.getAction()));
        }

        public BusinessEventsAssertions<T> fromEventsMatching(Predicate<BusinessEventDataProvider> predicate) {
            final var filtered = recordedEvents.stream()
                .filter(predicate)
                .toList();

            return new BusinessEventsAssertions<>(and(), filtered);
        }

        @SuppressWarnings("unchecked")
        public BusinessEventsAssertions<Object> and() {
            return root == null ? (BusinessEventsAssertions<Object>) this : root;
        }

        public BusinessEventsAssertions<T> allEventsSatisfy(ThrowingConsumer<BusinessEventDataProvider> requirements) {
            recordedEvents.forEach(requirements);
            return this;
        }

        @SuppressWarnings("unchecked")
        public BusinessEventsAssertions<T> allPayloadsSatisfy(ThrowingConsumer<T> requirements) {
            return allEventsSatisfy(event -> {
                final var payload = (T) event.getPayload();
                requirements.accept(payload);
            });
        }

        public BusinessEventsAssertions<T> eventsWhereEmittedWithPayloads(Collection<?> payloads) {
            numberOfEventsEmitted(payloads.size());
            return allPayloadsSatisfy(payload -> Assertions.assertThat(payload).isIn(payloads));
        }

        @SafeVarargs
        public final BusinessEventsAssertions<T> eventsWhereEmittedWithPayloads(T firstPayload, T... additionalPayloads) {
            final var allPayloads = new ArrayList<>(additionalPayloads.length + 1);
            allPayloads.add(firstPayload);
            allPayloads.addAll(Arrays.asList(additionalPayloads));

            return eventsWhereEmittedWithPayloads(allPayloads);
        }

        public BusinessEventsAssertions<T> exactlyOneEventWasEmittedWithPayload(@NonNull T expectedPayload) {
            oneEventHasBeenEmitted();
            return eventsWhereEmittedWithPayloads(expectedPayload);
        }

        public BusinessEventsAssertions<T> exactlyOneEventWasEmittedWithAction(String action) {
            oneEventHasBeenEmitted();
            return allEventsSatisfy(event -> Assertions.assertThat(event.getAction()).isEqualTo(action));
        }

        public BusinessEventsAssertions<T> numberOfEventsEmitted(int expectedNumberOfEvents) {
            Assertions.assertThat(recordedEvents).hasSize(expectedNumberOfEvents);
            return this;
        }

        public BusinessEventsAssertions<T> noEventHasBeenEmitted() {
            return numberOfEventsEmitted(0);
        }

        public BusinessEventsAssertions<T> oneEventHasBeenEmitted() {
            return numberOfEventsEmitted(1);
        }
    }
}
