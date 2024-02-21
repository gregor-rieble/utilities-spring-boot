package de.gcoding.boot.businessevents.emission.aspect;

import de.gcoding.boot.businessevents.emission.BusinessEventsFactory;
import jakarta.annotation.Nullable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import static java.util.Objects.requireNonNull;

@Aspect
public class BusinessEventEmitterAspect implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessEventEmitterAspect.class);
    private final BusinessEventsFactory businessEventsFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final int order;

    public BusinessEventEmitterAspect(
        @NonNull BusinessEventsFactory businessEventsFactory,
        @NonNull ApplicationEventPublisher eventPublisher,
        int order
    ) {
        this.businessEventsFactory = requireNonNull(businessEventsFactory);
        this.eventPublisher = requireNonNull(eventPublisher);
        this.order = order;
    }

    @Around("target(emittingSource) && @annotation(configuration)")
    public Object emitEvents(ProceedingJoinPoint joinPoint, Object emittingSource, EmitBusinessEvent configuration) throws Throwable {
        final var signature = joinPoint.getSignature();

        if (signature instanceof MethodSignature methodSignature) {
            LOG.debug("@EmitBusinessEvent: intercepted method call to {}", methodSignature);
            failIfMethodHasNoReturnType(methodSignature);

            final var originalReturnValue = joinPoint.proceed();
            final var numEmitted = emitEvents(originalReturnValue, emittingSource, methodSignature, configuration);
            LOG.debug("@EmitBusinessEvent: emitted {} events after method call to {}", numEmitted, methodSignature);

            return originalReturnValue;
        }

        throw new BusinessEventAspectUsageException("@EmitBusinessEvent annotation can only be used on methods, but was used on signature: " + signature);
    }

    private int emitEvents(@Nullable Object originalReturnValue, Object emittingSource, MethodSignature methodSignature, EmitBusinessEvent configuration) {
        final var eventsToBeEmitted = businessEventsFactory.createBusinessEvents(
            originalReturnValue,
            emittingSource,
            methodSignature,
            configuration
        );

        eventsToBeEmitted.forEach(eventPublisher::publishEvent);
        return eventsToBeEmitted.size();
    }

    private void failIfMethodHasNoReturnType(MethodSignature methodSignature) {
        final var returnType = methodSignature.getReturnType();

        if (returnType == Void.class || returnType == void.class) {
            throw new BusinessEventAspectUsageException(
                "@EmitBusinessEvent annotation can only be used on methods that return a value, " +
                    "but was used on method with a void return type: " + methodSignature
            );
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
