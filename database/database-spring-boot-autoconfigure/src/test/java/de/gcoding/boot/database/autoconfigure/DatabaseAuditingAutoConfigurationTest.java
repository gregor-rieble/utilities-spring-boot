package de.gcoding.boot.database.autoconfigure;

import de.gcoding.boot.database.auditing.FixedNameAuditorAware;
import de.gcoding.boot.database.auditing.OffsetDateTimeProvider;
import de.gcoding.boot.database.auditing.PrincipalNameAuditorAware;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.util.ProxyUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.AUDITOR_AWARE_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.DATE_TIME_PROVIDER_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.SPRING_JPA_AUDITING_HANDLER_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingProperties.PROPERTIES_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseAuditingAutoConfigurationTest {
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
            DatabaseAuditingAutoConfiguration.class
        ));

    @Test
    void whenDisabledThroughPropertiesAutoConfigurationWillNotLoad() {
        contextRunner.withPropertyValues(PROPERTIES_PATH + ".enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(DatabaseAuditingAutoConfiguration.class));
    }

    @Test
    void fixedNameAuditorAwareBeanIsCreatedWithoutSpringSecurity() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .withClassLoader(new FilteredClassLoader(SecurityContextHolder.class))
            .run(context -> assertThat(context)
                .hasSingleBean(AuditorAware.class)
                .getBean(AUDITOR_AWARE_BEAN_NAME, AuditorAware.class)
                .isInstanceOf(FixedNameAuditorAware.class));
    }

    @Test
    void principalNameAuditorAwareBeanIsCreatedWithSpringSecurity() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .run(context -> assertThat(context)
                .hasSingleBean(AuditorAware.class)
                .getBean(AUDITOR_AWARE_BEAN_NAME, AuditorAware.class)
                .isInstanceOf(PrincipalNameAuditorAware.class));
    }

    @Test
    void fixedNameAuditorAwareBeanCanBeOverriddenIfCorrectNameIsUsed() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .withClassLoader(new FilteredClassLoader(SecurityContextHolder.class))
            .withBean(AUDITOR_AWARE_BEAN_NAME, TestAuditorAware.class)
            .run(context -> assertThat(context)
                .hasSingleBean(AuditorAware.class)
                .getBean(AUDITOR_AWARE_BEAN_NAME, AuditorAware.class)
                .isInstanceOf(TestAuditorAware.class));
    }

    @Test
    void principalNameAuditorAwareBeanCanBeOverriddenIfCorrectNameIsUsed() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .withBean(AUDITOR_AWARE_BEAN_NAME, TestAuditorAware.class)
            .run(context -> assertThat(context)
                .hasSingleBean(AuditorAware.class)
                .getBean(AUDITOR_AWARE_BEAN_NAME, AuditorAware.class)
                .isInstanceOf(TestAuditorAware.class));
    }

    @Test
    void offsetDateTimeProviderBeanIsAvailable() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .run(context -> assertThat(context)
                .hasSingleBean(DateTimeProvider.class)
                .getBean(DATE_TIME_PROVIDER_BEAN_NAME, DateTimeProvider.class)
                .isInstanceOf(OffsetDateTimeProvider.class));
    }

    @Test
    void offsetDateTimeProviderBeanCanBeOverriddenIfCorrectNameIsUsed() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .withBean(DATE_TIME_PROVIDER_BEAN_NAME, TestDateTimeProvider.class)
            .run(context -> assertThat(context)
                .hasSingleBean(DateTimeProvider.class)
                .getBean(DATE_TIME_PROVIDER_BEAN_NAME, DateTimeProvider.class)
                .isInstanceOf(TestDateTimeProvider.class));
    }

    @Test
    void jpaAuditingHandlerIsRegisteredWithCorrectDateTimeProviderAndAuditorAwareBeans() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .run(context -> {
                final var beanAssert = assertThat(context)
                    .hasSingleBean(AuditingHandler.class)
                    .getBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME, AuditingHandler.class);

                beanAssert
                    .extracting("auditorAware")
                    .matches(auditorAware -> auditorAware instanceof Optional<?> o && ProxyUtils.getUserClass(o.orElseThrow()) == PrincipalNameAuditorAware.class);

                beanAssert
                    .extracting("dateTimeProvider")
                    .isInstanceOf(OffsetDateTimeProvider.class);
            });
    }

    @Test
    void configurationIsSkippedIfAuditingHandlerIsAlreadyPresent() {
        contextRunner
            .withBean(SPRING_JPA_AUDITING_HANDLER_BEAN_NAME, AuditingHandler.class)
            .run(context -> assertThat(context).doesNotHaveBean(DatabaseAuditingAutoConfiguration.class));
    }

    @Test
    void databaseAuditingPropertiesAreAvailable() {
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);

        contextRunner
            .run(context -> assertThat(context).hasSingleBean(DatabaseAuditingProperties.class));
    }

    static class TestAuditorAware implements AuditorAware<String> {
        @Nonnull
        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.empty();
        }
    }

    static class TestDateTimeProvider implements DateTimeProvider {
        @Nonnull
        @Override
        public Optional<TemporalAccessor> getNow() {
            return Optional.empty();
        }
    }
}