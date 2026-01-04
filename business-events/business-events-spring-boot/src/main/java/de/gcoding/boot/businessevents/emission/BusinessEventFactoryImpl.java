package de.gcoding.boot.businessevents.emission;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.EventActions;
import de.gcoding.boot.businessevents.emission.aspect.EmitBusinessEvent;
import jakarta.annotation.Nonnull;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public class BusinessEventFactoryImpl implements BusinessEventFactory {
    private static final Map<String, Expression> EXPRESSIONS_CACHE = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser;
    private final BeanResolver beanResolver;

    public BusinessEventFactoryImpl(@Nonnull SpelExpressionParser parser, @Nonnull BeanResolver beanResolver) {
        this.parser = requireNonNull(parser, "parser must not be null");
        this.beanResolver = requireNonNull(beanResolver, "beanResolver must not be null");
    }

    @Override
    public @Nonnull BusinessEvent createBusinessEvent(
        @Nonnull Object payload,
        @Nonnull Object wrappedPayload,
        @Nonnull Object emittingSource,
        @Nonnull MethodSignature methodSignature,
        @Nonnull EmitBusinessEvent configuration
    ) {
        final var actionResolver = new ActionEvaluator(payload, wrappedPayload, emittingSource, methodSignature, configuration);
        final var action = actionResolver.resolveAction();

        return BusinessEvent.withPayload(payload)
            .action(action)
            .build(emittingSource);
    }

    private class ActionEvaluator {
        private final Object payload;
        private final Object wrappedPayload;
        private final Object emittingSource;
        private final MethodSignature methodSignature;
        private final EmitBusinessEvent configuration;

        private ActionEvaluator(Object payload, Object wrappedPayload, Object emittingSource, MethodSignature methodSignature, EmitBusinessEvent configuration) {
            this.payload = payload;
            this.wrappedPayload = wrappedPayload;
            this.emittingSource = emittingSource;
            this.methodSignature = methodSignature;
            this.configuration = configuration;
        }

        public String resolveAction() {
            return Optional.ofNullable(configuration.actionSpEL())
                .filter(StringUtils::hasText)
                .map(this::evaluateActionAsSpEL)
                .filter(StringUtils::hasText)
                .orElseGet(this::resolveStaticAction);
        }

        private String resolveStaticAction() {
            return Optional.ofNullable(configuration.action())
                .filter(StringUtils::hasText)
                .orElse(EventActions.NONE);
        }

        private String evaluateActionAsSpEL(String actionSpEL) {
            final var expression = createExpression(actionSpEL);
            final var context = createEvaluationContext();

            return expression.getValue(context, String.class);
        }

        private Expression createExpression(String actionSpEL) {
            return EXPRESSIONS_CACHE.computeIfAbsent(actionSpEL, parser::parseExpression);
        }

        private StandardEvaluationContext createEvaluationContext() {
            final var context = new StandardEvaluationContext();
            final var rootObject = asActionEvaluationRoot();

            context.setBeanResolver(beanResolver);
            context.setRootObject(rootObject);

            return context;
        }

        private ActionEvaluationRoot asActionEvaluationRoot() {
            return new ActionEvaluationRoot(payload, wrappedPayload, emittingSource, methodSignature, configuration);
        }
    }

    private record ActionEvaluationRoot(
        Object payload,
        Object wrappedPayload,
        Object emittingSource,
        MethodSignature methodSignature,
        EmitBusinessEvent configuration
    ) {
    }
}
