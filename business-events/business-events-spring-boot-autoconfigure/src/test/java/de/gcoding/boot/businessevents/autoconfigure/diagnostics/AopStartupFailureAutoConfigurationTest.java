package de.gcoding.boot.businessevents.autoconfigure.diagnostics;

import de.gcoding.boot.diagnostics.DiagnosableException;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static de.gcoding.boot.businessevents.autoconfigure.diagnostics.AopStartupFailureAutoConfiguration.FailIfAopNotEnabled.AOP_DISABLED_MESSAGE;
import static de.gcoding.boot.businessevents.autoconfigure.diagnostics.AopStartupFailureAutoConfiguration.FailIfAopNotOnClasspath.AOP_NOT_ON_CLASSPATH_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

class AopStartupFailureAutoConfigurationTest {
    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AopStartupFailureAutoConfiguration.class));

    @Test
    void whenAopIsDisabledApplicationStartupFailsWithDiagnosableException() {
        contextRunner.withPropertyValues("spring.aop.auto=false").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause()
                .isInstanceOf(DiagnosableException.class)
                .hasMessage(AOP_DISABLED_MESSAGE);
        });
    }

    @Test
    void whenAopIsNotInClasspathApplicationStartupFailsWithDiagnosableException() {
        contextRunner.withClassLoader(new FilteredClassLoader(AnnotationAwareAspectJAutoProxyCreator.class)).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause()
                .isInstanceOf(DiagnosableException.class)
                .hasMessage(AOP_NOT_ON_CLASSPATH_MESSAGE);
        });
    }
}