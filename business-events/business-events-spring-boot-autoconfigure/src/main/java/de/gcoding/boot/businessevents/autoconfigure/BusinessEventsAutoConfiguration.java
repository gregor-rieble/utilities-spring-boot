package de.gcoding.boot.businessevents.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsProperties.PROPERTIES_PATH;

@AutoConfiguration
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@ImportAutoConfiguration({BusinessEventsEmissionAutoConfiguration.class, BusinessEventsListenAutoConfiguration.class})
@EnableConfigurationProperties(BusinessEventsProperties.class)
public class BusinessEventsAutoConfiguration {
}
