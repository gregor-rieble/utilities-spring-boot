package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.listen.BusinessEventListenerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListenerFactory;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsListenProperties.PROPERTIES_PATH;

@AutoConfiguration
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BusinessEventsListenProperties.class)
public class BusinessEventsListenAutoConfiguration {
    public static final String EVENT_LISTENER_FACTORY_BEAN_NAME = "businessEventsEventListenerFactory";

    @Bean(name = EVENT_LISTENER_FACTORY_BEAN_NAME)
    public EventListenerFactory businessEventsEventListenerFactory(BeanFactory beanFactory) {
        return new BusinessEventListenerFactory(beanFactory);
    }
}
