package de.gcoding.boot.businessevents.listen;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.BusinessEventDataProvider;
import de.gcoding.boot.businessevents.EventActions;
import de.gcoding.boot.businessevents.listen.BusinessEventListenerAnnotationIT.BusinessEventListenerAnnotationTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.verify;

@SpringBootTest(classes = BusinessEventListenerAnnotationTestConfiguration.class)
class BusinessEventListenerAnnotationIT {
    @SpyBean
    private MockBusinessEventListenerService listenerService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void whenAnnotatedMethodIsFoundItIsInvokedOnDesiredEvent() {
        final var event = BusinessEvent.withPayload("payload").action(EventActions.CREATE).build();

        eventPublisher.publishEvent(event);

        verify(listenerService).onBusinessEvent(event, "payload", EventActions.CREATE);
    }

    public static class MockBusinessEventListenerService {
        @BusinessEventListener
        public void onBusinessEvent(BusinessEventDataProvider event, Object payload, String action) {
        }
    }

    @Configuration
    public static class BusinessEventListenerAnnotationTestConfiguration {
        @Bean
        public BusinessEventListenerFactory businessEventListenerFactory(BeanFactory beanFactory) {
            return new BusinessEventListenerFactory(beanFactory);
        }

        @Bean
        public MockBusinessEventListenerService mockBusinessEventListenerService() {
            return new MockBusinessEventListenerService();
        }
    }
}
