package de.gcoding.boot.businessevents.listen;


import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.BusinessEventDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;

public class BusinessEventListenerMethodAdapter implements ApplicationListener<BusinessEvent> {
    public static final Logger LOG = LoggerFactory.getLogger(BusinessEventListenerMethodAdapter.class);
    private final BusinessEventListener configuration;
    private final Supplier<Object> methodOwnerSupplier;
    private final Method method;
    private final Map<Integer, ArgumentValueExtractor> argumentBindings = new HashMap<>();

    public BusinessEventListenerMethodAdapter(BusinessEventListener configuration, Supplier<Object> methodOwnerSupplier, Method method) {
        this.configuration = configuration;
        this.methodOwnerSupplier = methodOwnerSupplier;
        this.method = method;

        createArgumentBindings();
    }

    @Override
    public void onApplicationEvent(@NonNull BusinessEvent event) {
        if (shouldBeInvokedForEvent(event)) {
            LOG.debug("Invoking @BusinessEventListener annotated method {} for BusinessEvent {}", method, event);
            invokeAnnotatedMethod(event);
        }
    }

    private boolean shouldBeInvokedForEvent(BusinessEventDataProvider event) {
        return isPayloadTypeIsRequested(configuration.payloadType(), event.getPayload())
            && isActionRequested(configuration.actions(), event.getAction());
    }

    private void invokeAnnotatedMethod(BusinessEventDataProvider event) {
        final var methodParameterTypes = method.getParameterTypes();
        final var arguments = createMethodArguments(methodParameterTypes, event);
        final var target = methodOwnerSupplier.get();

        sneakyThrows(() -> method.invoke(target, arguments));
    }

    private boolean isPayloadTypeIsRequested(Class<?> requestedPayloadType, Object payload) {
        final var payloadType = payload.getClass();
        return requestedPayloadType.isAssignableFrom(payloadType);
    }

    private boolean isActionRequested(String[] requestedActions, String actualAction) {
        final var allActionsShouldBeIncluded = requestedActions.length == 0;
        if (allActionsShouldBeIncluded) {
            return true;
        }

        return Arrays.asList(requestedActions).contains(actualAction);
    }

    private Object[] createMethodArguments(Class<?>[] methodParameterTypes, BusinessEventDataProvider event) {
        final var arguments = new Object[methodParameterTypes.length];

        for (var i = 0; i < arguments.length; i++) {
            final var binding = argumentBindings.get(i);
            final var value = binding.extractArgumentValue(event);
            arguments[i] = value;
        }

        return arguments;
    }

    private void createArgumentBindings() {
        bindBusinessEventArgument();
        bindPayloadArgument();
        bindActionArgument();

        failIfNotAllArgumentsAreBound();
    }

    private void bindBusinessEventArgument() {
        final var binder = new PredicateCheckingArgumentBinder(cls -> cls == BusinessEventDataProvider.class, event -> event);
        iterateUnboundMethodParametersAndTryToBindArgument(binder);
    }

    private void bindPayloadArgument() {
        final var payloadType = configuration.payloadType();

        final var binder = new PredicateCheckingArgumentBinder(cls -> cls.isAssignableFrom(payloadType), BusinessEventDataProvider::getPayload);
        iterateUnboundMethodParametersAndTryToBindArgument(binder);
    }

    private void bindActionArgument() {
        final var binder = new PredicateCheckingArgumentBinder(cls -> cls == String.class, BusinessEventDataProvider::getAction);
        iterateUnboundMethodParametersAndTryToBindArgument(binder);
    }

    private void iterateUnboundMethodParametersAndTryToBindArgument(ArgumentBinder argumentBinder) {
        final var parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            final var isAlreadyBound = argumentBindings.containsKey(i);
            if (isAlreadyBound) {
                continue;
            }

            final var parameterType = parameterTypes[i];
            final var bindingResult = argumentBinder.tryToBindToArgumentOfType(parameterType);

            if (bindingResult.isPresent()) {
                final var binder = bindingResult.get();
                argumentBindings.put(i, binder);

                break;
            }
        }
    }

    private void failIfNotAllArgumentsAreBound() {
        final var parameterTypes = method.getParameterTypes();
        final var errors = new LinkedList<String>();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!argumentBindings.containsKey(i)) {
                errors.add("Argument at position " + i + " of type " + parameterTypes[i] + " cannot be bound to " +
                    "business event, payload nor action");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Illegal usage of @BusinessEventListener annotation. " +
                "Annotated method " + method + " has parameters that cannot be bound: " +
                StringUtils.collectionToCommaDelimitedString(errors));
        }
    }

    @FunctionalInterface
    private interface ArgumentBinder {
        Optional<ArgumentValueExtractor> tryToBindToArgumentOfType(Class<?> parameterType);
    }

    private record PredicateCheckingArgumentBinder(
        Predicate<Class<?>> canBeBoundTest,
        ArgumentValueExtractor argumentValueExtractor
    ) implements ArgumentBinder {
        @Override
        public Optional<ArgumentValueExtractor> tryToBindToArgumentOfType(Class<?> parameterType) {
            if (canBeBoundTest.test(parameterType)) {
                return Optional.of(argumentValueExtractor);
            }

            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface ArgumentValueExtractor {
        Object extractArgumentValue(BusinessEventDataProvider event);
    }
}
