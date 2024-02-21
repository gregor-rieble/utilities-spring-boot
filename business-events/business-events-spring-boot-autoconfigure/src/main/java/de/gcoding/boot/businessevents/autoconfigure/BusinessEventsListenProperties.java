package de.gcoding.boot.businessevents.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsListenProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class BusinessEventsListenProperties {
    public static final String PROPERTIES_PATH = BusinessEventsProperties.PROPERTIES_PATH + ".listen";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
