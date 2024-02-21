package de.gcoding.boot.businessevents.emission.aspect;

import de.gcoding.boot.businessevents.BusinessEventsException;

public class BusinessEventAspectUsageException extends BusinessEventsException {
    public BusinessEventAspectUsageException(String message) {
        super(message);
    }
}
