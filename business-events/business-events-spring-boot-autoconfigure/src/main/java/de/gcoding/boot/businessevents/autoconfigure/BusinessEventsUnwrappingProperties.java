package de.gcoding.boot.businessevents.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsUnwrappingProperties.PROPERTIES_PATH;

@ConfigurationProperties(PROPERTIES_PATH)
public class BusinessEventsUnwrappingProperties {
    public static final String PROPERTIES_PATH = BusinessEventsEmissionProperties.PROPERTIES_PATH + ".unwrapping";

    /**
     * Enables or disables the unwrapping functionality. If disabled, no event payload unwrapping takes place. For
     * example, return values of type {@code Optional} and {@code Collection} of annotated methods will be used as
     * they are for the event payloads
     */
    private boolean enabled = true;

    /**
     * Used to enable or disable specific payload unwrapper
     */
    private UnwrapProperties unwrap = new UnwrapProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UnwrapProperties getUnwrap() {
        return unwrap;
    }

    public void setUnwrap(UnwrapProperties unwrap) {
        this.unwrap = unwrap;
    }

    public static class UnwrapProperties {
        /**
         * Enables or disables unwrapping for {@code Optional} typed return values
         */
        private boolean optionals = true;

        /**
         * Enables or disables unwrapping for {@code Collection} typed return values
         */
        private boolean collections = true;

        public boolean isOptionals() {
            return optionals;
        }

        public void setOptionals(boolean optionals) {
            this.optionals = optionals;
        }

        public boolean isCollections() {
            return collections;
        }

        public void setCollections(boolean collections) {
            this.collections = collections;
        }
    }
}
