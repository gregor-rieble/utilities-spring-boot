package de.gcoding.boot.businessevents.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventsAutoConfigurationTest {
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            BusinessEventsAutoConfiguration.class,
            AopAutoConfiguration.class
        ));

    @Test
    void whenEnabledChildAutoConfigurationsAreActive() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsEmissionAutoConfiguration.class)
            .hasSingleBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenDisabledChildAutoConfigurationsAreNotAvailable() {
        contextRunner.withPropertyValues("gcoding.business-events.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsEmissionAutoConfiguration.class)
            .doesNotHaveBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenEnabledExplicitlyChildAutoConfigurationsAreActive() {
        contextRunner.withPropertyValues("gcoding.business-events.enabled=true").run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsEmissionAutoConfiguration.class)
            .hasSingleBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenEnabledBusinessEventPropertiesAreAvailable() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsProperties.class));
    }

    @Test
    void whenDisabledBusinessEventPropertiesAreNotAvailable() {
        contextRunner.withPropertyValues("gcoding.business-events.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsProperties.class));
    }
}
