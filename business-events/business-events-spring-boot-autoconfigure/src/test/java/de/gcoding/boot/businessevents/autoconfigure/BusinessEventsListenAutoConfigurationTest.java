package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.listen.BusinessEventListenerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.event.EventListenerFactory;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsListenAutoConfiguration.EVENT_LISTENER_FACTORY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventsListenAutoConfigurationTest {
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BusinessEventsListenAutoConfiguration.class));

    @Test
    void whenEnabledAutoConfigurationsAreActive() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenDisabledAutoConfigurationsAreInactive() {
        contextRunner.withPropertyValues("gcoding.business-events.listen.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenEnabledExplicitlyAutoConfigurationsAreActive() {
        contextRunner.withPropertyValues("gcoding.business-events.listen.enabled=true").run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsListenAutoConfiguration.class));
    }

    @Test
    void whenEnabledBusinessEventsListenPropertiesAreAvailable() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(BusinessEventsListenProperties.class));
    }

    @Test
    void whenEnabledEventListenerFactoryIsAvailable() {
        contextRunner.run(context -> assertThat(context)
            .getBean(EVENT_LISTENER_FACTORY_BEAN_NAME, EventListenerFactory.class)
            .isInstanceOf(BusinessEventListenerFactory.class));
    }

    @Test
    void whenDisabledEventListenerFactoryIsNotAvailable() {
        contextRunner.withPropertyValues("gcoding.business-events.listen.enabled=false").run(context -> assertThat(context)
            .doesNotHaveBean(EVENT_LISTENER_FACTORY_BEAN_NAME));
    }
}