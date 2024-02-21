package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.autoconfigure.diagnostics.AopStartupFailureAutoConfiguration;
import de.gcoding.boot.businessevents.emission.BusinessEventFactory;
import de.gcoding.boot.businessevents.emission.BusinessEventFactoryImpl;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactory;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactoryImpl;
import de.gcoding.boot.businessevents.emission.aspect.BusinessEventEmitterAspect;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsEmissionProperties.PROPERTIES_PATH;
import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingAutoConfiguration.PRIMARY_UNWRAPPER_BEAN_NAME;

@AutoConfiguration
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BusinessEventsEmissionProperties.class)
@ImportAutoConfiguration({BusinessEventsUnwrappingAutoConfiguration.class, AopStartupFailureAutoConfiguration.class})
public class BusinessEventsEmissionAutoConfiguration {
    public static final String EXPRESSION_PARSER_BEAN_NAME = "businessEventsExpressionParser";
    public static final String BEAN_RESOLVER_BEAN_NAME = "businessEventsBeanResolver";

    @Bean
    public BusinessEventEmitterAspect businessEventEmitterAspect(
        BusinessEventsFactory businessEventsFactory,
        ApplicationEventPublisher eventPublisher,
        BusinessEventsEmissionProperties properties
    ) {
        final var order = properties.getAspect().getOrder();

        return new BusinessEventEmitterAspect(businessEventsFactory, eventPublisher, order);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessEventsFactory businessEventsFactory(
        @Autowired(required = false) @Qualifier(PRIMARY_UNWRAPPER_BEAN_NAME) EventPayloadUnwrapper eventPayloadUnwrapper,
        BusinessEventFactory businessEventFactory
    ) {
        if (eventPayloadUnwrapper == null) {
            eventPayloadUnwrapper = EventPayloadUnwrapper.NOOP;
        }

        return new BusinessEventsFactoryImpl(eventPayloadUnwrapper, businessEventFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessEventFactory businessEventFactory(
        @Qualifier(EXPRESSION_PARSER_BEAN_NAME) @Autowired(required = false) SpelExpressionParser expressionParser,
        @Qualifier(BEAN_RESOLVER_BEAN_NAME) @Autowired(required = false) BeanResolver beanResolver,
        BeanFactory beanFactory
    ) {
        if (expressionParser == null) {
            expressionParser = new SpelExpressionParser();
        }

        if (beanResolver == null) {
            beanResolver = new BeanFactoryResolver(beanFactory);
        }

        return new BusinessEventFactoryImpl(expressionParser, beanResolver);
    }

}
