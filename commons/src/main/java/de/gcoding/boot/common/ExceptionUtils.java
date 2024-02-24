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
     * Executes the given {@code callable} and catches all exceptions from the {@code callable} and rethrows them
     * in an unchecked way by &quot;tricking&quot; the compiler.
     *
     * @param callable The callable to be executed
     * @param <T>      The result type of the callable
     * @return The unmodified result of the callable
     */
    public static <T> T sneakyThrows(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            rethrowChecked(e);
        }
        throw new UnreachableCodeException();
    }

    /**
     * Same as {@link #sneakyThrows(Callable)} but without the need to return a value within your {@code runnable}.
     *
     * @param runnable The runnable to be executed
     */
    public static void sneakyThrows(ThrowingRunnable runnable) {
        runnable.run();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrowChecked(Throwable t) throws T {
        throw (T) t;
    }
}
