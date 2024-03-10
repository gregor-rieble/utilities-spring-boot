package de.gcoding.boot.database.autoconfigure.diagnostics;

import de.gcoding.boot.diagnostics.DiagnosableException;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.AUDITOR_AWARE_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingAutoConfiguration.SPRING_JPA_AUDITING_HANDLER_BEAN_NAME;
import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingProperties.PROPERTIES_PATH;
import static de.gcoding.boot.diagnostics.DiagnosisDetails.withDescription;

@AutoConfiguration
@ConditionalOnBean(name = SPRING_JPA_AUDITING_HANDLER_BEAN_NAME)
@ConditionalOnMissingBean(name = AUDITOR_AWARE_BEAN_NAME)
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
public class JpaAuditingConfiguredFailureAutoConfiguration {
    public static final String MULTIPLE_AUDITING_CONFIGURATIONS_MESSAGE = "It looks like you already have an auditing " +
        "configuration provided in your application. The database-spring-boot-starter project tries to register an " +
        "additional auditing configuration, which will not work.";

    @PostConstruct
    public void fail() {
        // @formatter:off
        throw new DiagnosableException(
            withDescription(MULTIPLE_AUDITING_CONFIGURATIONS_MESSAGE)
                .andSuggestedActions()
                    .of("Set " + PROPERTIES_PATH + ".enabled=false to use your already existing auditing configuration")
                    .of("Remove or disable your already existing auditing configuration (typically enabled by using the" +
                        "@EnableJpaAuditing annotation)")
                    .build());
        // @formatter:on
    }
}
