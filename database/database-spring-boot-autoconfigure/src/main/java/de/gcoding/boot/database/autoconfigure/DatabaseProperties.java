package de.gcoding.boot.database.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

import java.util.Objects;

import static de.gcoding.boot.database.autoconfigure.DatabaseProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class DatabaseProperties {
    public static final String PROPERTIES_PATH = "gcoding.database";

    /**
     * Whether to enable the database starter functionalities such as auto registering AuditorAware and
     * DateTimeProvider instances. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * Configures the behaviour of the auditing functionality
     */
    private DatabaseAuditingProperties auditing = new DatabaseAuditingProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NonNull
    public DatabaseAuditingProperties getAuditing() {
        return auditing;
    }

    public void setAuditing(@NonNull DatabaseAuditingProperties databaseAuditingProperties) {
        this.auditing = Objects.requireNonNull(databaseAuditingProperties);
    }

}
