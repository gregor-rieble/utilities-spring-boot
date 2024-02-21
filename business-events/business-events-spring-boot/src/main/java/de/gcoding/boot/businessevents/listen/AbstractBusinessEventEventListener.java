package de.gcoding.boot.businessevents.listen;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.EventActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBusinessEventEventListener<T> implements ApplicationListener<BusinessEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBusinessEventEventListener.class);
    private final Class<T> desiredPayloadType;
    private final Map<String, BiConsumer<T, BusinessEvent>> callbacks = new HashMap<>();

    protected AbstractBusinessEventEventListener(@NonNull Class<T> desiredPayloadType) {
        this.desiredPayloadType = requireNonNull(desiredPayloadType);

        registerCallback(this::internalOnCreate, EventActions.CREATE);
        registerCallback(this::internalOnUpdate, EventActions.UPDATE);
        registerCallback(this::internalOnDelete, EventActions.DELETE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(BusinessEvent event) {
        final var payload = event.getPayload();

        if (isPayloadOfDesiredType(payload)) {
            final var typedPayload = (T) payload;
            final var action = event.getAction();

            LOG.debug("Received event with id {} and with action {} that is of desired type {}", event.getId(), action, desiredPayloadType);
            executeCallbackDependingOnAction(action, typedPayload, event);
        }
    }

    private void internalOnCreate(T entity, BusinessEvent event) {
        LOG.debug("onCreate called for event with id {} and action {}", event.getId(), event.getAction());
        onCreate(entity, event);
    }

    protected void onCreate(T entity, BusinessEvent event) {
        // do nothing by default, can be overridden by subclasses
    }

    private void internalOnUpdate(T entity, BusinessEvent event) {
        LOG.debug("onUpdate called for event with id {} and action {}", event.getId(), event.getAction());
        onUpdate(entity, event);
    }

    protected void onUpdate(T entity, BusinessEvent event) {
        // do nothing by default, can be overridden by subclasses
    }

    private void internalOnDelete(T entity, BusinessEvent event) {
        LOG.debug("onDelete called for event with id {} and action {}", event.getId(), event.getAction());
        onDelete(entity, event);
    }

    protected void onDelete(T entity, BusinessEvent event) {
        // do nothing by default, can be overridden by subclasses
    }

    private void internalOnUnhandledAction(String action, T entity, BusinessEvent event) {
        LOG.debug("onUnhandledAction called for event with id {} and action {}", event.getId(), action);
        onUnhandledAction(action, entity, event);
    }

    protected void onUnhandledAction(String action, T entity, BusinessEvent event) {
        // do nothing by default, can be overridden by subclasses
    }

    @NonNull
    protected BiConsumer<T, BusinessEvent> resolveCallback(String action) {
        return callbacks.getOrDefault(
            action,
            (entity, event) -> internalOnUnhandledAction(action, entity, event)
        );
    }

    protected final void registerCallback(@NonNull BiConsumer<T, BusinessEvent> callback, @NonNull String... actions) {
        for (final var action : actions) {
            callbacks.put(action, callback);
        }
    }

    private void executeCallbackDependingOnAction(String action, T typedPayload, BusinessEvent event) {
        final var callback = resolveCallback(action);
        LOG.debug("Resolved callback for event with id {} and action {} to {}", event.getId(), action, callback);

        callback.accept(typedPayload, event);
    }

    private boolean isPayloadOfDesiredType(Object payload) {
        if (payload == null) {
            return false;
        }

        final var actualPayloadType = payload.getClass();
        return desiredPayloadType.isAssignableFrom(actualPayloadType);
    }
}
