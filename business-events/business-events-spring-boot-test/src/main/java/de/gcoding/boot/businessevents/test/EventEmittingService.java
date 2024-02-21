package de.gcoding.boot.businessevents.test;


import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EventEmittingService {
    private final EventActionService eventActionService;

    public EventEmittingService(EventActionService eventActionService) {
        this.eventActionService = eventActionService;
    }

    @EmitBusinessEvent
    public String emitSimpleEvent(String payload) {
        return payload;
    }

    @EmitBusinessEvent
    public Optional<String> emitOptionalEvent(String payload) {
        return Optional.ofNullable(payload);
    }

    @EmitBusinessEvent
    public List<String> emitEventsForEachListItem(List<String> payloads) {
        return payloads;
    }

    @EmitBusinessEvent
    public Set<String> emitEventsForEachSetItem(Set<String> payloads) {
        return payloads;
    }

    @EmitBusinessEvent
    public Collection<String> emitEventsForEachCollectionItem(Collection<String> payloads) {
        return payloads;
    }

    @Transactional
    @EmitBusinessEvent
    public String emitEventWithinSpringTransactional(String payload) {
        return payload;
    }

    @jakarta.transaction.Transactional
    @EmitBusinessEvent
    public String emitEventWithinJakartaTransactional(String payload) {
        return payload;
    }

    @EmitBusinessEvent(action = "custom_action")
    public String emitEventWithAction() {
        return "custom_action";
    }

    @EmitBusinessEvent(actionSpEL = "payload")
    public String emitEventWithSpELAction() {
        return "dynamic_action";
    }

    @EmitBusinessEvent(actionSpEL = "@eventActionService.action")
    public String emitEventWithSpELActionAccessingBeans() {
        return eventActionService.getAction();
    }
}
