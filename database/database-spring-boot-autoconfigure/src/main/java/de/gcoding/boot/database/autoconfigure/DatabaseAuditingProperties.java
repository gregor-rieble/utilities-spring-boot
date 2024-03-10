package de.gcoding.boot.database.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static de.gcoding.boot.database.autoconfigure.DatabaseAuditingProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class DatabaseAuditingProperties {
    public static final String PROPERTIES_PATH = DatabaseProperties.PROPERTIES_PATH + ".auditing";

    /**
     * Whether to enable the auditing features such as auto registering AuditorAware and
     * DateTimeProvider instances. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * Overwrites the default system principal name for created by and modified by properties in case no auditor
     * can be extracted from the security context
     */
    private String systemPrincipal;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSystemPrincipal() {
        return systemPrincipal;
    }

    public void setSystemPrincipal(String systemPrincipal) {
        this.systemPrincipal = systemPrincipal;
    }
}
