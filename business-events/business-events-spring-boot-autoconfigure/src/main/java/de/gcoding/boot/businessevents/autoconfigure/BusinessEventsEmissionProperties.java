package de.gcoding.boot.businessevents.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsEmissionProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class BusinessEventsEmissionProperties {
    public static final String PROPERTIES_PATH = BusinessEventsProperties.PROPERTIES_PATH + ".emission";

    /**
     * Enable or Disable the event emission functionality through the {@code @EmitBusinessEvent} annotation
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Configuration properties to configure the aspect behavior
     */
    private AspectProperties aspect = new AspectProperties();

    public AspectProperties getAspect() {
        return aspect;
    }

    public void setAspect(AspectProperties aspect) {
        this.aspect = aspect;
    }

    public static class AspectProperties {
        /**
         * The order for the {@code @EmitBusinessEvent} annotation. By default, the order is set to the lowest precedence,
         * meaning that other aspects based on annotations used on the same method will be invoked first.
         */
        private int order = Ordered.LOWEST_PRECEDENCE;

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }
}
