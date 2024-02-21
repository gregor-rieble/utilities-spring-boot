package de.gcoding.boot.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionUtilsTest {
    @ParameterizedTest
    @ValueSource(classes = {IllegalStateException.class, IllegalArgumentException.class, RuntimeException.class})
    void whenSneakyThrowsCatchesARuntimeExceptionItIsRethrownAsIs(Class<? extends Exception> exceptionType) throws Exception {
        final var expectedException = exceptionType.getConstructor().newInstance();
        final var callable = givenACallableThatThrowsTheException(expectedException);

        final var rethrownException = assertThrows(
            exceptionType,
            () -> ExceptionUtils.sneakyThrows(callable)
        );

        assertThat(rethrownException).isSameAs(expectedException);
    }

    @ParameterizedTest
    @ValueSource(classes = {IOException.class, CloneNotSupportedException.class, GeneralSecurityException.class})
    void whenSneakyThrowsCatchesACheckedExceptionItIsWrappedInAnIllegalStateExceptionWithTheSameMessage(Class<? extends Exception> exceptionType) throws Exception {
        final var checkedException = exceptionType.getConstructor().newInstance();
        final var callable = givenACallableThatThrowsTheException(checkedException);

        final var exception = assertThrows(
            IllegalStateException.class,
            () -> ExceptionUtils.sneakyThrows(callable)
        );

        assertThat(exception)
            .hasMessage(checkedException.getMessage())
            .hasCause(checkedException);
    }

    @Test
    void whenSneakyThrowsCatchesAnInterruptedExceptionTheCurrentThreadIsInterrupted() {
        final var callable = givenACallableThatThrowsTheException(new InterruptedException("message"));

        try {
            ExceptionUtils.sneakyThrows(callable);
        } catch (IllegalStateException e) {
            assertThat(Thread.interrupted()).isTrue();
        }
    }

    @Test
    void whenSneakyThrowsCatchesAnInterruptedExceptionTheInterruptedExceptionIsSetAsCause() {
        final var interruptedException = new InterruptedException("message");
        final var callable = givenACallableThatThrowsTheException(interruptedException);

        final var exception = assertThrows(
            IllegalStateException.class,
            () -> ExceptionUtils.sneakyThrows(callable)
        );

        assertThat(exception)
            .hasMessage(interruptedException.getMessage())
            .hasCause(interruptedException);
    }

    private Callable<Void> givenACallableThatThrowsTheException(Exception e) {
        return () -> {
            throw e;
        };
    }
}