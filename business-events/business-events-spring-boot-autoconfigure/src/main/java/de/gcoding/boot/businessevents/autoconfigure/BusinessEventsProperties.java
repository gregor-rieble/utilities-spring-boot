package de.gcoding.boot.businessevents.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class BusinessEventsProperties {
    public static final String PROPERTIES_PATH = "gcoding.business-events";

    /**
     * Enables or disables the use of the Business Events functionality. If disabled, no events will be emitted
     * from methods annotated with {@code @EmitBusinessEvent} and subscriptions using {@code @BusinessEventListener}
     * will have no effect
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
