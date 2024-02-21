package de.gcoding.boot.common;

import java.util.concurrent.Callable;

/**
 * Contains utility functions for handling and working with exceptions
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
        // utility class should not be instantiated
    }

    /**
     * Executes the given {@code callable} and catches all exceptions and rethrows them according to the following
     * rules:
     *
     * <ul>
     *     <li>
     *         If the caught exception is a {@link RuntimeException}, it is rethrown without modification.
     *     </li>
     *     <li>
     *         If the caught exception is a {@link InterruptedException}, the current thread will be marked as interrupted
     *         before the exception is rethrown as described below
     *     </li>
     *     <li>
     *         If the caught exception is a checked exception, the exception is wrapped in an {@link IllegalStateException}
     *         before it is rethrown. The {@link IllegalStateException} will have the same message as the original exception
     *         and will have the original exception as its cause
     *     </li>
     * </ul>
     *
     * @param callable The callable to be executed
     * @param <T>      The result type of the callable
     * @return The unmodified result of the callable
     */
    public static <T> T sneakyThrows(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
