# business-events

<!-- @formatter:off -->
<!-- TOC -->
* [business-events](#business-events)
  * [Introduction](#introduction)
  * [Getting Started](#getting-started)
  * [Emit Events](#emit-events)
    * [Actions](#actions)
      * [Dynamic Actions](#dynamic-actions)
    * [Unwrapping](#unwrapping)
      * [Optional Unwrapping](#optional-unwrapping)
      * [Collection Unwrapping](#collection-unwrapping)
      * [Custom Unwrapping](#custom-unwrapping)
      * [Disable Unwrapping](#disable-unwrapping)
  * [Subscribe to Events](#subscribe-to-events)
    * [Annotation based subscription](#annotation-based-subscription)
      * [Parameter deconstruction](#parameter-deconstruction)
    * [Extend `AbstractBusinessEventListener`](#extend-abstractbusinesseventlistener)
    * [Spring Application Listener](#spring-application-listener)
  * [Configuration Properties](#configuration-properties)
<!-- TOC -->
<!-- @formatter:on -->

## Introduction

Adds the ability to emit and listen for spring application events using an annotation based approach with
`@EmitBusinessEvent` and `@BusinessEventListener`. This is particular useful, if you want to emit events
in case your entities or domain models change. For example, if users are created, updated or deleted, and
you would like to emit spring application events for these scenarios, this library is for you.

## Getting Started

Include the starter project in one of your spring boot applications to get started:

```xml

<dependency>
    <groupId>de.gcoding.boot</groupId>
    <artifactId>business-events-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

Now you can use `@EmitBusinessEvent` and `@BusinessEventListener` annotations with your custom payloads:

```java
public record User(int id, String username) {
}

@Service
public class UserService {
    @EmitBusinessEvent
    public User createUser() {
        return new User(1, "john.doe");
    }

    @BusinessEventListener(payloadType = User.class)
    public void onUserCreation(User createdUser) {
        System.out.println(createdUser);
    }
}
```

## Emit Events

Events will be emitted using the `BusinessEvent` type that extends springs `ApplicationEvent`. It consists of a payload
of arbitrary type and other metadata that can be adapted to your needs.

Annotate your bean/service method with `@EmitBusinessEvent`. The return value of that method will automatically be
wrapped as a payload in a `BusinessEvent` and emitted as a spring application event.

```java

@Service
public class MyService {
    @EmitBusinessEvent
    public String emitEvent() {
        return "payload value";
    }
}
```

The above invocation of `emitEvent` will emit a `BusinessEvent` that will contain the string value `payload value`
as its payload.

### Actions

Typically, you would also want to specify an action that describes how the model that was returned was mutated. For
example, imagine you have a `User` model and a `UserService` that allows you to create and delete users.
The following code demonstrates how you would be able to emit events with the according actions:

```java

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    @EmitBusinessEvent(action = EventActions.CREATE)
    public User createUser(String username) {
        return userRepository.save(new User(username));
    }

    @Transactional
    @EmitBusinessEvent(action = EventActions.DELETE)
    public Optional<User> deleteUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    userRepository.delete(user);
                    return user;
                });
    }
}
```

> **NOTE**: The `deleteUser` method returns an optional. If no user could be found for deletion, then no event will
> be emitted. Or in other words, only if a user was actually deleted, an event is emitted (see the unwrapping section
> below for more details)

> **NOTE**: By default, the event emission happens inside transactions, if the method is also annotated
> with `@Transactionsl`. If this is not desired, you can change the aspect order by specifying
> the `gcoding.business-events.emission.aspect.order` property

#### Dynamic Actions

Sometimes, the action that should be used must be derived dynamically. For example, in a service that has a method
`insertOrUpdateEntity`, you would want to emit an event with a `CREATE` action, in case the entity was inserted
and use a `UPDATE` action, when the entity was updated. This can be achieved with the `actionSpEL` property of the
`@EmitBusinessEvent` annotation.

```java

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    @EmitBusinessEvent(actionSpEL = "payload.version == 0 ? 'CREATE' : 'UPDATE'")
    public User insertOrCreateUser(User userEntity) {
        return userRepository.saveAndFlush(userEntity);
    }
}
```

The above example assumes that the `User` entity makes use of the `@Version` annotation, so that the version field
is `0` when a new entity is inserted and increases each time an update is made.

> If you specify `actionSpEL`, it will always have preference over the static action configured through the `action`
> property and render it useless

Within the SpEL, the following properties are made available in the root context for evaluation:

| Property          | Description                                                                                                                                                   |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `payload`         | The (potentially unwrapped) payload of the event (in the example above, it would be the `User` entity)                                                        |
| `emittingSource`  | The instance that defined the method on which this annotation was used and which was invoked (in the example above, this would be the `UserService` instance) |
| `methodSignature` | The signature of the method on which this annotation was used (in the example above, the `MethodSignature` of the `UserService#insertOrCreateUser` method)    |
| `configuration`   | The instance of the `@EmitBusinessEvent` annotation and the respective property values that where used                                                        |

### Unwrapping

Payloads emitted using `@EmitBusinessEvent` are unwrapped under some circumstances, so that the event-payloads that are
used for the event differ from the actual return value. Build-in unwrapping takes place for `Optional` and `Collection`
based return types as described below.

#### Optional Unwrapping

If your method returns an optional, event emission can be prevented, if the optional is empty

```java

@Service
public class MyService {
    @EmitBusinessEvent
    public Optional<MyPayload> emitEvent(boolean emit) {
        if (emit) {
            return Optional.of(new MyPayload());
        }

        return Optional.empty();
    }
}
```

If the method above is invoked with `emit=true`, then a `BusinessEvent` will be created that contains the `MyPayload`
instance
as payload (The payload will actually be the value of the optional and not the optional itself). On the other hand,
if `emit=false` is passed, an empty optional will be returned and thus no event will be emitted at all.

#### Collection Unwrapping

If your method returns a collection (e.g., `List`, `Set`, `...`), then an event is created for each item within the
returned
collection

```java

@Service
public class MyService {
    @EmitBusinessEvent
    public Collection<String> emitEvent() {
        return List.of("first", "second");
    }
}
```

The above method will emit 2 events. One with `first` as the payload and one with `second`.

#### Custom Unwrapping

You are allowed to provide your own unwrapping logic by providing beans of type `EventPayloadUnwrapper` to the
application context. They will be picked up automatically.

```java

@Component
public class CustomUnwrapper implements EventPayloadUnwrapper {
    @Override
    public Optional<Stream<Object>> unwrap(Object payload, Object emittingSource, MethodSignature methodSignature,
                                           EmitBusinessEvent configuration) {
        if (payload instanceof Pair pair) {
            // Emit 2 Events (for each entry of the pair)
            return Optional.of(Stream.of(pair.getFirst(), pair.getSecond()));
        } else if (payload instanceof KeyPair keyPair) {
            // Only use the public key as a payload for the emitted event
            return Optional.of(Stream.of(keyPair.getPublic()));
        }

        // allow other unwrapper to handle unwrapping by returning an empty optional
        return Optional.empty();
    }
}
```

> Please refer to the javadoc for more details on how to implement a custom unwrapper

> The custom unwrapper is also allowed to change the type of the payload or convert the payload as needed.
> For example, you could have an unwrapper that transforms a raw entity into a DTO before it is used as
> a event payload

#### Disable Unwrapping

Sometimes, you do not want unwrapping to take place for whatever reason. Maybe your payload should actually
be the list that was returned and not the individual entries. To disable unwrapping you have 4 options:

1. Use `@EmitBusinessEvent(skipUnwrap = true)`
2. Disable unwrapping globally through the unwrapping and unwrap properties as described in
   the [Configuration Properties](#configuration-properties) section
3. Provide a `EventPayloadUnwrapper` bean with the exact name `primaryEventPayloadUnwrapper`. It will be used as the
   primary unwrapper implementation and other unwrapper instances will be ignored.
4. Create a custom `EventPayloadUnwrapperListModifier` bean that will modify the list of available unwrapper instances.
   This will also take effect globally.

## Subscribe to Events

You have 3 options on how to subscribe to business events

1. Use the `@BusinessEventListener` annotation
2. Extend the `AbstractBusinessEventEventListener` class
3. Use springs built-in `ApplicationListener`

### Annotation based subscription

Similar to springs `@EventListener` annotation, you can use the `@BusinessEventListener` annotation in order
to conveniently subscribe to business events and deconstruct payloads as method arguments.

```java

@Service
public class ListenerService {
    @BusinessEventListener
    public void onBusinessEvent(BusinessEvent businessEvent) {
        System.out.println(businessEvent.getPayload());
    }
}
```

The above listener will subscribe to **ALL** business events regardless of the action or payload type.

If you would like to filter for specify event types and/or actions, use the `payloadType` and/or `actions`
annotation parameters:

```java

@Service
public class ListenerService {
    @BusinessEventListener(payloadType = User.class, actions = {"CREATE", "DELETE"})
    public void onBusinessEvent(User user, String action) {
        System.out.println("User " + user.id() + "modified with action: " + action);
    }
}
```

This example will only be invoked, if a business event with a payload of type `User` is emitted and if
the emitted event has either `CREATE` or `DELETE` as action.

#### Parameter deconstruction

If you use the annotation based approach with `@BusinessEventListener`, you can use the following parameters
in your method signature, and they will be injected for each event accordingly:

| Parameter Type   | Description                                                                                                                                                                   |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BusinessEvent`  | Will contain the original business event that was emitted                                                                                                                     |
| `<T>` (any type) | Will contain the payload data of the business event. Users must make sure, that the filter will only match for events which payloads can be cast to the desired target type.  |
| `String`         | Will contain the action that was used for the emitted event (Note, if you emit events with `String` as payload, the payload will take precedence for string typed parameters) |

Your annotated methods might have none or all of the parameters above.

> **NOTE**: Your IDE might create a warning if your annotated method has more than 1 parameter. This is because
> the `@BusinessEventListener` annotation has a `@EventListener` meta annotation. However, you can ignore this
> warning, your method is allowed to have 0 to 3 arguments, if you use `@BusinessEventListener`.

### Extend `AbstractBusinessEventListener`

This approach comes in handy when you would like a single class handling specific payloads and their corresponding
actions such as `CREATE`, `UPDATE` or `DELETE` (or your custom actions).

For example, lets say we want to have a listener for `User` modifications. Additionally, we emit a business
event for the users right after the register by using the custom `REGISTER` action. The following listener
can be used for this scenario.

```java

@Component
public class UserModificationHandler extends AbstractBusinessEventListener {
    public UserModificationHandler() {
        super(User.class); // subscribe for events with User payloads

        // register our own action and make the listener execute the onRegister method for this action
        registerCallback(this::onRegister, "REGISTER");
    }

    @Override
    protected void onCreate(User createdUser, BusinessEvent event) {
        System.out.println("Received event with CREATE action");
    }

    @Override
    protected void onUpdate(User updatedUser, BusinessEvent event) {
        System.out.println("Received event with UPDATE action");
    }

    @Override
    protected void onDelete(User deletedUser, BusinessEvent event) {
        System.out.println("Received event with DELETE action");
    }

    @Override
    protected void onRegister(User registeredUser, BusinessEvent event) {
        System.out.println("Received event with REGISTER action");
    }

    @Override
    protected void onUnhandledAction(String action, User user, BusinessEvent event) {
        System.out.println("Received event with unhandled action: " + action);
    }
}
```

### Spring Application Listener

Because business events are simple spring application events, you can use the same mechanisms as with any
other spring application event. Simply implement the `ApplicationListener` interface with `BusinessEvent` as
the generic type

```java

@Component
public class CustomBusinessEventListener implements ApplicationListener<BusinessEvent> {
    @Override
    public void onApplicationEvent(BusinessEvent event) {
        System.out.println("Received business event with action " + event.getAction() +
                " and payload " + event.getPayload());
    }
}
```

> **NOTE**: If you use this approach, you must filter the business events according to your needs by yourself

## Configuration Properties

| Property                                                         | Description                                                                                                                                                                                                                                 | Default Value               |
|------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `gcoding.business-events.enabled`                                | Enables or disables the use of the Business Events functionality. If disabled, no events will be emitted from methods annotated with `@EmitBusinessEvent` and subscriptions using `@BusinessEventListener` will have no effect              | `true`                      |
| `gcoding.business-events.emission.enabled`                       | Enable or Disable the event emission functionality through the `@EmitBusinessEvent` annotation                                                                                                                                              | `true`                      |
| `gcoding.business-events.emission.aspect.order`                  | The order for the `@EmitBusinessEvent` annotation. By default, the order is set to the lowest precedence, meaning that other aspects based on annotations used on the same method will be invoked first.                                    | `Ordered.LOWEST_PRECEDENCE` |
| `gcoding.business-events.emission.unwrapping.enabled`            | Enables or disables the unwrapping functionality. If disabled, no event payload unwrapping takes place. For example, return values of type `Optional` and `Collection` of annotated methods will be used as they are for the event payloads | `true`                      |
| `gcoding.business-events.emission.unwrapping.unwrap.optionals`   | Enables or disables unwrapping for `Optional` typed return values                                                                                                                                                                           | `true`                      |
| `gcoding.business-events.emission.unwrapping.unwrap.collections` | Enables or disables unwrapping for `Collection` typed return values                                                                                                                                                                         | `true`                      |