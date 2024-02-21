package de.gcoding.boot.businessevents.listen;

import de.gcoding.boot.businessevents.BusinessEvent;
import org.springframework.context.event.EventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated methods will automatically subscribe for {@link BusinessEvent}s that have
 * payloads of the specified {@link #payloadType()}. Annotated methods <em>might</em> have arguments of the
 * following types that will get injected with values as specified:
 * <dl>
 *     <dt>{@code BusinessEvent} event</dt>
 *     <dd>Will contain the business event that was received by the listener</dd>
 *     <dt>{@code String} action</dt>
 *     <dd>Will contain the action of the event</dd>
 *     <dt>{@code <T> payload}</dt>
 *     <dd>Will contain the events payload. The type must be the same as specified through {@link #payloadType()}</dd>
 * </dl>
 * Take the following code snipped as an example:
 * <pre>
 * &#064;Service
 * public class EventListenerService {
 *     &#064;BusinessEventListener(payloadType = User.class, actions = { EventActions.CREATE, EventActions.DELETE })
 *     public void fireOnBusinessEvent(BusinessEvent event, User user, String action) {
 *         System.out.println("Received user event of action " + action + ", with payload: " + user);
 *     }
 * }
 * </pre>
 * The {@code fireOnBusinessEvent} will be invoked for each business event that has one of the action "CREATE" or
 * "DELETE" and which has a payload type (or subtype) of "User"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@EventListener
public @interface BusinessEventListener {
    /**
     * Filter events by their payload type. Subtype payloads will also be matched
     *
     * @return The type of payload for which a listener should be installed
     */
    Class<?> payloadType() default Object.class;

    /**
     * Filter events by actions. The annotated method will only be invoked if the event's action is one of the
     * specified actions here.
     *
     * @return The actions that events must have in order for this listener to fire
     */
    String[] actions() default {};
}
