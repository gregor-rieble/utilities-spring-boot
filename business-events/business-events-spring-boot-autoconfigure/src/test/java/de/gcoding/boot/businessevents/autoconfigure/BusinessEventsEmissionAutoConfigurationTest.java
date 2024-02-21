package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingAutoConfigurationTest.CustomEventPayloadUnwrapper;
import de.gcoding.boot.businessevents.autoconfigure.diagnostics.AopStartupFailureAutoConfiguration;
import de.gcoding.boot.businessevents.emission.BusinessEventFactory;
import de.gcoding.boot.businessevents.emission.BusinessEventFactoryImpl;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactory;
import de.gcoding.boot.businessevents.emission.BusinessEventsFactoryImpl;
import de.gcoding.boot.businessevents.emission.aspect.BusinessEventEmitterAspect;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import de.gcoding.boot.businessevents.emission.unwrapper.CompositeEventPayloadUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.Ordered;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsEmissionAutoConfiguration.BEAN_RESOLVER_BEAN_NAME;
import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsEmissionAutoConfiguration.EXPRESSION_PARSER_BEAN_NAME;
import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingAutoConfiguration.PRIMARY_UNWRAPPER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventsEmissionAutoConfigurationTest {
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            BusinessEventsEmissionAutoConfiguration.class,
            AopAutoConfiguration.class
        ));

    @Test
    void whenEnabledChildAutoConfigurationsAreActive() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsUnwrappingAutoConfiguration.class)
            .hasSingleBean(AopStartupFailureAutoConfiguration.class));
    }

    @Test
    void whenDisabledChildAutoConfigurationsAreNotAvailable() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsUnwrappingAutoConfiguration.class)
            .doesNotHaveBean(AopStartupFailureAutoConfiguration.class));
    }

    @Test
    void whenEnabledAutoConfigurationsAreActive() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsEmissionAutoConfiguration.class));
    }

    @Test
    void whenDisabledAutoConfigurationsAreInactive() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsEmissionAutoConfiguration.class));
    }

    @Test
    void whenEnabledExplicitlyAutoConfigurationsAreActive() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.enabled=true").run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsEmissionAutoConfiguration.class));
    }

    @Test
    void whenEnabledBusinessEventsEmissionPropertiesAreAvailable() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsEmissionProperties.class));
    }

    @Test
    void whenDisabledBusinessEventsEmissionPropertiesAreNotAvailable() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsEmissionProperties.class));
    }

    @Test
    void whenEnabledBusinessEventEmitterAspectBeanIsAvailableAndConfiguredProperly() {
        contextRunner.run(context -> {
            final var beanAssertions = assertThat(context)
                .getBean(BusinessEventEmitterAspect.class);

            beanAssertions
                .extracting("businessEventsFactory")
                .isInstanceOf(BusinessEventsFactoryImpl.class);

            beanAssertions.extracting("eventPublisher").isNotNull();
            beanAssertions.hasFieldOrPropertyWithValue("order", Ordered.LOWEST_PRECEDENCE);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {Ordered.HIGHEST_PRECEDENCE, Ordered.LOWEST_PRECEDENCE, 0, -10, 50})
    void whenAspectOrderIsSpecifiedItIsUsedInAspectBean(int order) {
        contextRunner.withPropertyValues("gcoding.business-events.emission.aspect.order=" + order).run(context -> assertThat(context)
            .getBean(BusinessEventEmitterAspect.class)
            .hasFieldOrPropertyWithValue("order", order));
    }

    @Test
    void whenCustomBusinessEventsFactoryIsSpecifiedItIsUsedInstead() {
        contextRunner.withBean(BusinessEventsFactory.class, CustomEventsFactory::new).run(context -> {
            assertThat(context)
                .getBean(BusinessEventEmitterAspect.class)
                .extracting("businessEventsFactory")
                .isInstanceOf(CustomEventsFactory.class);

            assertThat(context)
                .hasSingleBean(BusinessEventsFactory.class)
                .getBean(BusinessEventsFactory.class)
                .isInstanceOf(CustomEventsFactory.class);
        });
    }

    @Test
    void whenEnabledBusinessEventsFactoryBeanIsAvailableAndConfiguredProperly() {
        contextRunner.run(context -> {
            final var beanAssertions = assertThat(context)
                .getBean(BusinessEventsFactory.class)
                .isInstanceOf(BusinessEventsFactoryImpl.class);

            beanAssertions
                .extracting("businessEventFactory")
                .isInstanceOf(BusinessEventFactoryImpl.class);

            beanAssertions
                .extracting("eventPayloadUnwrapper")
                .isInstanceOf(CompositeEventPayloadUnwrapper.class);
        });
    }

    @Test
    void whenCustomPrimaryUnwrapperIsAvailableItIsConfiguredAccordingly() {
        contextRunner.withBean(PRIMARY_UNWRAPPER_BEAN_NAME, EventPayloadUnwrapper.class, CustomEventPayloadUnwrapper::new)
            .run(context -> assertThat(context)
                .getBean(BusinessEventsFactory.class)
                .extracting("eventPayloadUnwrapper")
                .isInstanceOf(CustomEventPayloadUnwrapper.class));
    }

    @Test
    void whenUnwrappingIsDisabledANoopUnwrapperIsUsed() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.unwrapping.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(EventPayloadUnwrapper.class)
                .getBean(BusinessEventsFactory.class)
                .extracting("eventPayloadUnwrapper")
                .isEqualTo(EventPayloadUnwrapper.NOOP));
    }

    @Test
    void whenEnabledBusinessEventFactoryBeanIsAvailableAndConfiguredProperly() {
        contextRunner.run(context -> {
            final var beanAssertions = assertThat(context)
                .getBean(BusinessEventFactory.class)
                .isInstanceOf(BusinessEventFactoryImpl.class);

            beanAssertions
                .extracting("parser")
                .matches(parser -> parser.getClass() == SpelExpressionParser.class);

            beanAssertions
                .extracting("beanResolver")
                .isInstanceOf(BeanFactoryResolver.class);
        });
    }

    @Test
    void whenCustomExpressionParserIsPresentWithSpecificBeanNameItWillBeUsedAccordingly() {
        contextRunner.withBean(EXPRESSION_PARSER_BEAN_NAME, SpelExpressionParser.class, CustomExpressionParser::new)
            .run(context -> assertThat(context)
                .getBean(BusinessEventFactory.class)
                .extracting("parser")
                .isInstanceOf(CustomExpressionParser.class));
    }

    @Test
    void whenCustomBeanResolverIsPresentWithSpecificBeanNameItWillBeUsedAccordingly() {
        contextRunner.withBean(BEAN_RESOLVER_BEAN_NAME, BeanResolver.class, CustomBeanResolver::new)
            .run(context -> assertThat(context)
                .getBean(BusinessEventFactory.class)
                .extracting("beanResolver")
                .isInstanceOf(CustomBeanResolver.class));
    }

    @Test
    void whenAopIsDisabledAndEmissionIsDisabledApplicationStartsNormally() {
        contextRunner.withPropertyValues("spring.aop.auto=false", "gcoding.business-events.emission.enabled=false")
            .run(context -> assertThat(context).hasNotFailed());
    }

    public static class CustomEventsFactory implements BusinessEventsFactory {
        @Nonnull
        @Override
        public List<BusinessEvent> createBusinessEvents(@Nullable Object payload, @Nonnull Object emittingSource, @Nonnull MethodSignature methodSignature, @Nonnull EmitBusinessEvent configuration) {
            return List.of();
        }
    }

    public static class CustomExpressionParser extends SpelExpressionParser {
    }

    public static class CustomBeanResolver implements BeanResolver {
        @Nonnull
        @Override
        public Object resolve(@Nonnull EvaluationContext context, @Nonnull String beanName) {
            return new Object();
        }
    }
}