package de.gcoding.boot.businessevents.emission.aspect;

import de.gcoding.boot.businessevents.BusinessEvent;
import de.gcoding.boot.businessevents.EventActions;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Optional;

/**
 * <p>
 * When used on methods with a return type, will emit a {@link BusinessEvent}
 * through the standard {@link ApplicationEventPublisher}. The business event will
 * have the return value as its payload and the action as specified through {@link #action()}
 * or {@link #actionSpEL()}
 * </p>
 * <p>
 * Before the business event will be created, the return value will - if not configured
 * otherwise - be unwrapped for {@link Optional} and {@link Collection} return types. Meaning that
 * if an Optional is encountered and the Optional is not empty, the value of the Optional will be
 * used as the payload instead of the Optional itself. On the other hand, if the Optional is empty,
 * no event will be emitted at all. If a Collection is encountered as the return type, a business event
 * will be emitted for each entry of the collection. Therefore, if the collection is empty, no event will
 * be emitted at all.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EmitBusinessEvent {
    /**
     * The action for the emitted events. Note that if you provide a blank value, the action will
     * default to {@link EventActions#NONE}. Setting the action here will have no effect, if you also
     * specify {@link #actionSpEL()}, which will always take precedence.
     *
     * @return The action to be used for the events. {@link EventActions#NONE}, by default
     */
    String action() default EventActions.NONE;

    /**
     * Can be used to disable the unwrapping behavior of Optional and Collection
     * type payloads. If {@code true}, all Optionals and Collections will be used as
     * the events payload without unwrapping.
     *
     * @return {@code true}, if unwrapping should be skipped, default is {@code false}
     */
    boolean skipUnwrap() default false;

    /**
     * If a static action is not sufficient for your use case, you can configure a SpEL for evaluating the action
     * using this property. The root context will contain the following variables:
     * <dl>
     *     <dt>{@code payload}</dt>
     *     <dd>The (potentially unwrapped) payload of the event</dd>
     *     <dt>{@code emittingSource}</dt>
     *     <dd>The instance that defined the method on which this annotation was used and which was invoked</dd>
     *     <dt>{@code methodSignature}</dt>
     *     <dd>The signature of the method on which this annotation was used</dd>
     *     <dt>{@code configuration}</dt>
     *     <dd>The instance of the {@link EmitBusinessEvent} annotation which triggered the event emission</dd>
     * </dl>
     * If the SpEL will evaluate to a blank value, the action will default to {@link EventActions#NONE}
     *
     * @return The SpEL that should be used to evaluate the events action
     */
    String actionSpEL() default "";
}
