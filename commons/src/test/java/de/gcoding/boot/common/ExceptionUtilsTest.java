package de.gcoding.boot.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionUtilsTest {
    @ParameterizedTest
    @ValueSource(classes = {IllegalStateException.class, IllegalArgumentException.class, RuntimeException.class, IOException.class, InterruptedException.class, CloneNotSupportedException.class, GeneralSecurityException.class})
    void whenSneakyThrowsCatchesAnExceptionItIsRethrownAsIs(Class<? extends Exception> exceptionType) throws Exception {
        final var expectedException = exceptionType.getConstructor().newInstance();
        final var callable = givenACallableThatThrowsTheException(expectedException);

        final var rethrownException = assertThrows(
            exceptionType,
            () -> sneakyThrows(callable)
        );

        assertThat(rethrownException).isSameAs(expectedException);
    }

    @ParameterizedTest
    @ValueSource(classes = {IllegalStateException.class, IllegalArgumentException.class, RuntimeException.class, IOException.class, InterruptedException.class, CloneNotSupportedException.class, GeneralSecurityException.class})
    void whenSneakyThrowsWithRunnableCatchesAnExceptionItIsRethrownAsIs(Class<? extends Exception> exceptionType) throws Exception {
        final var expectedException = exceptionType.getConstructor().newInstance();
        final var runnable = givenARunnableThatThrowsTheException(expectedException);

        final var rethrownException = assertThrows(
            exceptionType,
            () -> sneakyThrows(runnable)
        );

        assertThat(rethrownException).isSameAs(expectedException);
    }

    private Callable<Void> givenACallableThatThrowsTheException(Exception e) {
        return () -> {
            throw e;
        };
    }

    private ThrowingRunnable givenARunnableThatThrowsTheException(Exception e) {
        return () -> {
            throw e;
        };
    }
}