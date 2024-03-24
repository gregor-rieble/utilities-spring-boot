package de.gcoding.boot.database.autoconfigure;

import de.gcoding.boot.database.auditing.FixedNameAuditorAware;
import de.gcoding.boot.database.auditing.OffsetDateTimeProvider;
import de.gcoding.boot.database.auditing.PrincipalNameAuditorAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;

import static de.gcoding.boot.database.auditing.FixedNameAuditorAware.DEFAULT_SYSTEM_AUDITOR;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingProperties.PROPERTIES_PATH;

@AutoConfiguration
@EnableConfigurationProperties(DatabaseAuditingProperties.class)
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(name = DatabaseAuditingAutoConfiguration.SPRING_JPA_AUDITING_HANDLER_BEAN_NAME)
@EnableJpaAuditing(auditorAwareRef = DatabaseAuditingAutoConfiguration.AUDITOR_AWARE_BEAN_NAME, dateTimeProviderRef = DatabaseAuditingAutoConfiguration.DATE_TIME_PROVIDER_BEAN_NAME)
public class DatabaseAuditingAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseAuditingAutoConfiguration.class);
    public static final String SPRING_JPA_AUDITING_HANDLER_BEAN_NAME = "jpaAuditingHandler";
    public static final String AUDITOR_AWARE_BEAN_NAME = "databaseAuditorAware";
    public static final String DATE_TIME_PROVIDER_BEAN_NAME = "databaseDateTimeProvider";
    public static final String DATE_TIME_PROVIDER_CLOCK_BEAN_NAME = "databaseDateTimeProviderClock";

    @ConditionalOnClass(SecurityContextHolder.class)
    public static class SecurityAwareAuditorAutoConfiguration {
        @Bean(name = AUDITOR_AWARE_BEAN_NAME)
        @ConditionalOnMissingBean(name = AUDITOR_AWARE_BEAN_NAME)
        public AuditorAware<String> principalNameAuditorAware(DatabaseAuditingProperties auditingProperties) {
            final var systemPrincipal = auditingProperties.getSystemPrincipal();

            if (systemPrincipal == null) {
                LOG.info("Creating PrincipalNameAuditorAware with default system auditor {}", DEFAULT_SYSTEM_AUDITOR);
                return new PrincipalNameAuditorAware();
            } else {
                LOG.info("Creating PrincipalNameAuditorAware with default system auditor {}", systemPrincipal);
                return new PrincipalNameAuditorAware(systemPrincipal);
            }
        }
    }

    @ConditionalOnMissingClass("org.springframework.security.core.context.SecurityContextHolder")
    public static class FixedAuditorAutoConfiguration {
        @Bean(name = AUDITOR_AWARE_BEAN_NAME)
        @ConditionalOnMissingBean(name = AUDITOR_AWARE_BEAN_NAME)
        public AuditorAware<String> principalNameAuditorAware(DatabaseAuditingProperties auditingProperties) {
            final var systemPrincipal = auditingProperties.getSystemPrincipal();

            if (systemPrincipal == null) {
                LOG.info("Creating a FixedNameAuditorAware with the default system auditor {}", DEFAULT_SYSTEM_AUDITOR);
                return new FixedNameAuditorAware();
            } else {
                LOG.info("Creating a FixedNameAuditorAware with the system auditor {}", systemPrincipal);
                return new FixedNameAuditorAware(systemPrincipal);
            }
        }
    }

    @Bean(name = DATE_TIME_PROVIDER_BEAN_NAME)
    @ConditionalOnMissingBean(name = DATE_TIME_PROVIDER_BEAN_NAME)
    public DateTimeProvider databaseDateTimeProvider(
        @Qualifier(DATE_TIME_PROVIDER_CLOCK_BEAN_NAME) @Autowired(required = false) Clock databaseDateTimeProviderClock
    ) {
        if (databaseDateTimeProviderClock == null) {
            LOG.info("Creating a OffsetDateTimeProvider that uses the system clock");
            return new OffsetDateTimeProvider();
        } else {
            LOG.info("Creating a OffsetDateTimeProvider that uses the provided {} bean {}", DATE_TIME_PROVIDER_CLOCK_BEAN_NAME, databaseDateTimeProviderClock);
            return new OffsetDateTimeProvider(databaseDateTimeProviderClock);
        }
    }
}
