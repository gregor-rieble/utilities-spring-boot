package de.gcoding.boot.businessevents.listen;

import de.gcoding.boot.businessevents.BusinessEventDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;
import java.util.Arrays;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessEventListenerFactoryTest {
    static final String TEST_CLASS_BEAN_NAME = "testClass";
    ConfigurableBeanFactory beanFactory;
    BusinessEventListenerFactory businessEventListenerFactory;

    @BeforeEach
    void beforeEach() {
        beanFactory = new DefaultListableBeanFactory();
        businessEventListenerFactory = new BusinessEventListenerFactory(beanFactory);
    }

    @ParameterizedTest
    @ValueSource(strings = {"annotatedMethod", "annotatedMethodWithArgument"})
    void whenMethodIsAnnotatedWithBusinessEventListenerAnnotationTheFactorySupportsIt(String methodName) {
        final var method = givenTheTestClassMethod(methodName);

        final var result = businessEventListenerFactory.supportsMethod(method);

        assertThat(result).isTrue();
    }

    @Test
    void whenSuperMethodIsAnnotatedWithBusinessEventListenerAnnotationTheFactorySupportsIt() {
        final var method = givenTheTestClassMethod(ExtendingTestClass.class, "annotatedMethod");

        final var result = businessEventListenerFactory.supportsMethod(method);

        assertThat(result).isTrue();
    }

    @Test
    void whenMethodIsAnnotatedWithEventListenerTheFactoryDoesNotSupportIt() {
        final var method = givenTheTestClassMethod(ExtendingTestClass.class, "standardEventListener");

        final var result = businessEventListenerFactory.supportsMethod(method);

        assertThat(result).isFalse();
    }

    @Test
    void whenMethodIsNotAnnotatedTheFactoryDoesNotSupportIt() {
        final var method = givenTheTestClassMethod(ExtendingTestClass.class, "nonAnnotatedMethod");

        final var result = businessEventListenerFactory.supportsMethod(method);

        assertThat(result).isFalse();
    }

    @Test
    void whenFactoryIsMisusedWithNonAnnotatedMethodAnExceptionIsThrown() {
        final var method = givenTheTestClassMethod(ExtendingTestClass.class, "nonAnnotatedMethod");

        assertThrows(
            NullPointerException.class,
            () -> businessEventListenerFactory.createApplicationListener(TEST_CLASS_BEAN_NAME, Test.class, method)
        );
    }

    @Test
    void whenOrderIsRequestedHighestPrecedenceIsReturned() {
        final var order = businessEventListenerFactory.getOrder();

        assertThat(order).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void whenListenerIsCreatedItIsOfTypeBusinessEventListenerMethodAdapter() {
        final var method = givenTheTestClassMethod(ExtendingTestClass.class, "annotatedMethod");

        final var result = businessEventListenerFactory.createApplicationListener(TEST_CLASS_BEAN_NAME, Test.class, method);

        assertThat(result).isInstanceOf(BusinessEventListenerMethodAdapter.class);
    }

    private Method givenTheTestClassMethod(String methodName) {
        return givenTheTestClassMethod(TestClass.class, methodName);
    }

    private Method givenTheTestClassMethod(Class<?> clazz, String methodName) {
        return sneakyThrows(() ->
            Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .findAny()
                .orElseThrow()
        );
    }

    public static class TestClass {
        @BusinessEventListener
        public void annotatedMethod() {
        }

        @BusinessEventListener
        public void annotatedMethodWithArgument(BusinessEventDataProvider event) {
        }

        @EventListener
        public void standardEventListener() {
        }

        public void nonAnnotatedMethod() {
        }
    }

    public static class ExtendingTestClass extends TestClass {
        @Override
        public void annotatedMethod() {
        }
    }
}