package de.gcoding.boot.businessevents.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BusinessEventsTestConfiguration {
    @Bean
    public BusinessEventRecorder businessEventRecorder() {
        return new BusinessEventRecorder();
    }

    @Bean
    public EventEmittingService eventEmittingService(EventActionService eventActionService) {
        return new EventEmittingService(eventActionService);
    }

    @Bean
    public EventActionService eventActionService() {
        return new EventActionService();
    }
}
