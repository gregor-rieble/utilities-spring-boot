package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.emission.unwrapper.CollectionUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.CompositeEventPayloadUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.OptionalUnwrapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingProperties.PROPERTIES_PATH;

@AutoConfiguration
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BusinessEventsUnwrappingProperties.class)
public class BusinessEventsUnwrappingAutoConfiguration {
    public static final String PRIMARY_UNWRAPPER_BEAN_NAME = "primaryEventPayloadUnwrapper";

    @Primary
    @Bean(name = PRIMARY_UNWRAPPER_BEAN_NAME)
    @ConditionalOnMissingBean(name = PRIMARY_UNWRAPPER_BEAN_NAME)
    public EventPayloadUnwrapper primaryEventPayloadUnwrapper(List<EventPayloadUnwrapper> unwrapper) {
        return new CompositeEventPayloadUnwrapper(unwrapper);
    }

    @Bean
    @ConditionalOnProperty(value = PROPERTIES_PATH + ".unwrap.optionals", havingValue = "true", matchIfMissing = true)
    public EventPayloadUnwrapper optionalEventPayloadUnwrapper() {
        return new OptionalUnwrapper();
    }

    @Bean
    @ConditionalOnProperty(value = PROPERTIES_PATH + ".unwrap.collections", havingValue = "true", matchIfMissing = true)
    public EventPayloadUnwrapper collectionEventPayloadUnwrapper() {
        return new CollectionUnwrapper();
    }
}
