package de.gcoding.boot.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingRunnableTest {
    ThrowingRunnable runnable;

    @Test
    void throwingRunnableCanThrowACheckedException() {
        runnable = () -> {
            throw new IOException();
        };

        assertThrows(
            IOException.class,
            runnable::run
        );
    }
}