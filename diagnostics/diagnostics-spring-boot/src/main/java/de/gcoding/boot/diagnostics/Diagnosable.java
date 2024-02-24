package de.gcoding.boot.diagnostics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an exception as diagnosable, meaning that a spring boot application that encounters the annotated exception
 * during startup will print helpful details on how to solve the issue. Annotated exceptions will be handled by the
 * {@link DiagnosableFailureAnalyzer} in the same way as {@link DiagnosableException}s but with the description and
 * action taken from the annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Diagnosable {
    /**
     * Describes why the application could not be started
     *
     * @return The reason why the application failed to start
     */
    String description();

    /**
     * Describes the action(s) to take in order to solve the startup error
     *
     * @return The suggested action(s) in order to solve the failure of the application start
     */

    String action();
}
