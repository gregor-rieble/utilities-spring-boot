package de.gcoding.boot.businessevents;

/**
 * Utility class that contains constants for standard event actions such as CREATE or DELETE. An event action
 * typically describes what happened to a domain object or entity. E.g. if a user was created in the database it
 * would make sense to emit a business event with the CREATE action.
 */
public final class EventActions {
    /**
     * Entity or domain object has been created
     */
    public static final String CREATE = "CREATE";
    /**
     * Entity or domain object has been updated
     */
    public static final String UPDATE = "UPDATE";
    /**
     * Entity or domain object has been deleted
     */
    public static final String DELETE = "DELETE";
    /**
     * No detailed information are available on what exactly happened with the entity or domain model
     */
    public static final String NONE = "NONE";

    private EventActions() {
        // should not be instantiated
    }
}

