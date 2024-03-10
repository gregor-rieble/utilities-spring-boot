package de.gcoding.boot.database.autoconfigure;

import de.gcoding.boot.database.autoconfigure.diagnostics.JpaAuditingConfiguredFailureAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import static de.gcoding.boot.database.autoconfigure.DatabaseProperties.PROPERTIES_PATH;

@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(value = PROPERTIES_PATH + ".enabled", havingValue = "true", matchIfMissing = true)
@Import({DatabaseAuditingAutoConfiguration.class, JpaAuditingConfiguredFailureAutoConfiguration.class})
public class DatabaseAutoConfiguration {
}
