package de.gcoding.boot.database.autoconfigure.diagnostics;

import de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration;
import de.gcoding.boot.diagnostics.DiagnosableException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.mapping.context.PersistentEntities;

import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.SPRING_JPA_AUDITING_HANDLER_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.diagnostics.JpaAuditingConfiguredFailureAutoConfiguration.MULTIPLE_AUDITING_CONFIGURATIONS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaAuditingConfiguredFailureAutoConfigurationTest {
    @Mock
    PersistentEntities persistentEntities;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    Metamodel metamodel;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(EntityManagerFactory.class, () -> entityManagerFactory)
        .withBean(PersistentEntities.class, () -> persistentEntities)
        .withConfiguration(AutoConfigurations.of(
            JpaAuditingConfiguredFailureAutoConfiguration.class
        ));

    @Test
    void configurationIsNotLoadedIfNoJpaAuditingHandlerIsPresent() {
        contextRunner.run(context -> assertThat(context)
            .doesNotHaveBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME)
            .doesNotHaveBean(JpaAuditingConfiguredFailureAutoConfiguration.class));
    }

    @Test
    void configurationIsNotLoadedIfJpaAuditingHandlerIsPresentFromAuditingAutoConfiguration() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .withConfiguration(AutoConfigurations.of(DatabaseAuditingAutoConfiguration.class))
            .run(context -> assertThat(context)
                .hasBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME)
                .doesNotHaveBean(JpaAuditingConfiguredFailureAutoConfiguration.class));
    }

    @Test
    void configurationIsLoadedAndStartupFailsIfMultipleAuditingConfigurationsArePresent() {
        contextRunner
            .withBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME, AuditingHandler.class)
            .withConfiguration(AutoConfigurations.of(DatabaseAuditingAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).rootCause()
                    .isInstanceOf(DiagnosableException.class)
                    .hasMessage(MULTIPLE_AUDITING_CONFIGURATIONS_MESSAGE);
            });
    }

    @Test
    void configurationIsNotLoadedIfAuditingIsDisabledThroughProperties() {
        contextRunner
            .withBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME, AuditingHandler.class)
            .withConfiguration(AutoConfigurations.of(DatabaseAuditingAutoConfiguration.class))
            .withPropertyValues("gcoding.database.auditing.enabled=false")
            .run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean(JpaAuditingConfiguredFailureAutoConfiguration.class)
                .hasBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME)
                .hasSingleBean(AuditingHandler.class));
    }
}