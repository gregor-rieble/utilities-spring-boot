package de.gcoding.boot.common;

public class UnreachableCodeException extends IllegalStateException {
    public UnreachableCodeException() {
        super("This piece of code should never have been executed. Please report this error & share the stack " +
            "trace with the developers of the application.");
    }
}
