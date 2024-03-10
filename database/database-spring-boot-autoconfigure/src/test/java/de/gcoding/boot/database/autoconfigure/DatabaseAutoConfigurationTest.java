package de.gcoding.boot.database.autoconfigure;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseAutoConfigurationTest {
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    Metamodel metamodel;
    
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(EntityManagerFactory.class, () -> entityManagerFactory)
        .withConfiguration(AutoConfigurations.of(
            DatabaseAutoConfiguration.class
        ));

    @Test
    void autoConfigurationDoesNotLoadWhenDisabledThroughProperty() {
        contextRunner.withPropertyValues("gcoding.database.enabled=false")
            .run(context ->
                assertThat(context).doesNotHaveBean(DatabaseAutoConfiguration.class));
    }

    @Test
    void autoConfigurationLoadsWithoutActivelyEnablingIt() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner.run(context ->
            assertThat(context).hasSingleBean(DatabaseAutoConfiguration.class));
    }

    @Test
    void autoConfigurationLoadsWhenActivelyEnabled() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner.withPropertyValues("gcoding.database.enabled=true").run(context ->
            assertThat(context).hasSingleBean(DatabaseAutoConfiguration.class));
    }

    @Test
    void whenAutoConfigurationLoadsDatabasePropertiesAreAvailable() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner.run(context ->
            assertThat(context).hasSingleBean(DatabaseProperties.class));
    }

    @Test
    void whenAutoConfigurationLoadsChildConfigurationsAreLoaded() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner.run(context ->
            assertThat(context)
                .hasSingleBean(DatabaseAuditingAutoConfiguration.class));
    }
}
