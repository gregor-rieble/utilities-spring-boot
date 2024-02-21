package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import de.gcoding.boot.businessevents.emission.unwrapper.CollectionUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.CompositeEventPayloadUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.EventPayloadUnwrapper;
import de.gcoding.boot.businessevents.emission.unwrapper.OptionalUnwrapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;
import java.util.stream.Stream;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingAutoConfiguration.PRIMARY_UNWRAPPER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventsUnwrappingAutoConfigurationTest {
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BusinessEventsUnwrappingAutoConfiguration.class));

    @Test
    void whenEnabledAutoConfigurationsAreActive() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsUnwrappingAutoConfiguration.class));
    }

    @Test
    void whenDisabledAutoConfigurationsAreInactive() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.unwrapping.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsUnwrappingAutoConfiguration.class)
            .doesNotHaveBean(EventPayloadUnwrapper.class));
    }

    @Test
    void whenEnabledExplicitlyAutoConfigurationsAreActive() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.unwrapping.enabled=true").run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsUnwrappingAutoConfiguration.class));
    }

    @Test
    void whenEnabledBusinessEventsUnwrappingPropertiesAreAvailable() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsUnwrappingProperties.class));
    }

    @Test
    void whenEnabledPrimaryUnwrapperIsConfiguredProperly() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(OptionalUnwrapper.class)
            .hasSingleBean(CollectionUnwrapper.class)
            .hasBean(PRIMARY_UNWRAPPER_BEAN_NAME)
            .getBean(EventPayloadUnwrapper.class)
            .isInstanceOf(CompositeEventPayloadUnwrapper.class)
            .extracting("delegates").asList()
            .anyMatch(OptionalUnwrapper.class::isInstance)
            .anyMatch(CollectionUnwrapper.class::isInstance));
    }

    @Test
    void whenOptionalUnwrapperIsDisabledItIsNotUsed() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.unwrapping.unwrap.optionals=false").run(context -> assertThat(context)
            .doesNotHaveBean(OptionalUnwrapper.class)
            .getBean(EventPayloadUnwrapper.class)
            .isInstanceOf(CompositeEventPayloadUnwrapper.class)
            .extracting("delegates").asList()
            .noneMatch(OptionalUnwrapper.class::isInstance)
            .anyMatch(CollectionUnwrapper.class::isInstance));
    }

    @Test
    void whenCollectionUnwrapperIsDisabledItIsNotUsed() {
        contextRunner.withPropertyValues("gcoding.business-events.emission.unwrapping.unwrap.collections=false").run(context -> assertThat(context)
            .doesNotHaveBean(CollectionUnwrapper.class)
            .getBean(EventPayloadUnwrapper.class)
            .isInstanceOf(CompositeEventPayloadUnwrapper.class)
            .extracting("delegates").asList()
            .noneMatch(CollectionUnwrapper.class::isInstance)
            .anyMatch(OptionalUnwrapper.class::isInstance));
    }

    @Test
    void whenCustomUnwrapperBeanExistsWithNameSameAsPrimaryTheDefaultPrimaryUnwrapperIsOverridden() {
        contextRunner.withBean(PRIMARY_UNWRAPPER_BEAN_NAME, EventPayloadUnwrapper.class, CustomEventPayloadUnwrapper::new)
            .run(context -> {
                assertThat(context)
                    .doesNotHaveBean(CompositeEventPayloadUnwrapper.class)
                    .hasSingleBean(CustomEventPayloadUnwrapper.class)
                    .hasSingleBean(CollectionUnwrapper.class)
                    .hasSingleBean(OptionalUnwrapper.class)
                    .getBean(PRIMARY_UNWRAPPER_BEAN_NAME)
                    .isInstanceOf(CustomEventPayloadUnwrapper.class);
            });
    }

    public static class CustomEventPayloadUnwrapper implements EventPayloadUnwrapper {
        @Nonnull
        @Override
        public Optional<Stream<Object>> unwrap(@Nullable Object payload, @Nonnull Object emittingSource, @Nonnull MethodSignature methodSignature, @Nonnull EmitBusinessEvent configuration) {
            return Optional.empty();
        }
    }
}