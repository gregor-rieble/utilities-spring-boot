package de.gcoding.boot.common;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;

/**
 * A runnable that allows to throw exceptions in their run method
 */
@FunctionalInterface
public interface ThrowingRunnable extends Runnable {
    /**
     * Automatically called by {@link #run()}. Runs this operation and allows the implementor
     * to throw exceptions
     *
     * @throws Exception In case the operation fails
     */
    @SuppressWarnings("java:S112")
    void tryRun() throws Exception;

    /**
     * {@inheritDoc}
     * The default implementation calls {@link #tryRun()} which enables implementors
     * to define an operation that is allowed to throw exceptions
     */
    @Override
    default void run() {
        // due to tight coupling with sneakyThrows, we must make sure to invoke the sneaky throws method that
        // accepts a callable to prevent a stack overflow
        sneakyThrows(() -> {
            tryRun();
            return null;
        });
    }
}
