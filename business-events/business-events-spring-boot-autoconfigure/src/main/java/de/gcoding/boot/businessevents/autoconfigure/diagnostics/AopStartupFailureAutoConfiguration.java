package de.gcoding.boot.businessevents.autoconfigure.diagnostics;

import de.gcoding.boot.diagnostics.DiagnosableException;
import jakarta.annotation.PostConstruct;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static de.gcoding.boot.businessevents.autoconfigure.BusinessEventsEmissionProperties.PROPERTIES_PATH;
import static de.gcoding.boot.diagnostics.DiagnosisDetails.withDescription;

@AutoConfiguration
public class AopStartupFailureAutoConfiguration {
    private final Environment environment;
    private final AnnotationAwareAspectJAutoProxyCreator aspectCreator;

    public AopStartupFailureAutoConfiguration(Environment environment, @Autowired(required = false) AnnotationAwareAspectJAutoProxyCreator aspectCreator) {
        this.environment = environment;
        this.aspectCreator = aspectCreator;
    }

    @AutoConfiguration
    @ConditionalOnClass(AnnotationAwareAspectJAutoProxyCreator.class)
    public class FailIfAopNotEnabled {
        public static final String AOP_DISABLED_MESSAGE = "@EmitBusinessEvent annotation functionality needs spring " +
            "AOP to be enabled for it to work, but it seems to be disabled (we couldn't find a bean of type " +
            AnnotationAwareAspectJAutoProxyCreator.class.getName() + " in the application context)";

        @PostConstruct
        public void failIfAspectCreatorBeanNotFound() {
            if (aspectCreator == null) {
                // @formatter:off
                throw new DiagnosableException(
                    withDescription(AOP_DISABLED_MESSAGE)
                        .andMultipleOptionsOnHowToSolveTheIssue()
                        .withOption("Disable the business events aspect by setting " + PROPERTIES_PATH + ".enabled=false " +
                            "(Note that this will disable the @EmitBusinessEvent functionality)")
                        .withOption("Enable AOP by using the @EnableAspectJAutoProxy annotation on one of your configuration classes")
                        .withOption(this::actionIfAopIsDisabled)
                        .build());
                // @formatter:on
            }
        }

        private Optional<String> actionIfAopIsDisabled() {
            final var aopEnabled = environment.getProperty("spring.aop.auto", Boolean.class, true);
            if (!Boolean.TRUE.equals(aopEnabled)) {
                return Optional.of("Enable AOP by setting spring.aop.auto=true. Note that someone might have disabled it intentionally");
            }

            return Optional.empty();
        }
    }

    @AutoConfiguration
    @ConditionalOnMissingClass("org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator")
    public static class FailIfAopNotOnClasspath {
        public static final String AOP_NOT_ON_CLASSPATH_MESSAGE = "@EmitBusinessEvent annotation functionality needs " +
            "spring AOP to be enabled for it to work, however, the required AOP configuration does not seem to be " +
            "present on the classpath (we couldn't find the class " +
            "org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator)";

        @PostConstruct
        public void fail() {
            // @formatter:off
            throw new DiagnosableException(
                withDescription(AOP_NOT_ON_CLASSPATH_MESSAGE)
                    .andMultipleOptionsOnHowToSolveTheIssue()
                        .withOption("Add org.springframework.boot:spring-boot-starter-aop to your dependencies")
                        .withOption("Add org.springframework:spring-aop to your dependencies and use the @EnableAspectJAutoProxy " +
                            "on one of your configuration classes")
                        .build());
            // @formatter:on
        }
    }
}
