# database

<!-- @formatter:off -->
<!-- TOC -->
* [database](#database)
  * [Introduction](#introduction)
  * [Getting Started](#getting-started)
  * [Date Time Provider](#date-time-provider)
  * [Auditor Aware](#auditor-aware)
    * [System Auditor](#system-auditor)
  * [Using @EnableJpaAuditing](#using-enablejpaauditing)
  * [Configuration Properties](#configuration-properties)
<!-- TOC -->
<!-- @formatter:on -->

## Introduction

Autoconfigures common database auditing capabilities and adds an `AbstractBaseEntity` with built-in auditing
features such as creation date, last modification dates, versioning and user modification auditing.

## Getting Started

Include the starter project in one of your spring boot applications to get started:

```xml

<dependency>
    <groupId>de.gcoding.boot</groupId>
    <artifactId>database-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Create an entity that extends `AbstractBaseEntity` to get started:

```java

@Entity
@Table(name = "user")
public class UserEntity extends AbstractBaseEntity {
    @Column(nullable = false, unique = true)
    private String email;
}
```

This entity will now automatically have the following properties and features:

1. An `id` field that will have an automatically generated `UUID`
2. A `createdBy` field that will be filled with the principal name of the user that created the entity
3. A `created` field containing the `OffsetDateTime` of when the entity was created
4. A `modifiedBy` field that will be filled with the principal name of the user that last modified the entity
5. A `modified` field containing the `OffsetDateTime` of when the entity was last modified
6. A `version` field which is annotated with `@Version`. This field will be auto incremented each time the entity is
   updated

## Date Time Provider

A `DateTimeProvider` of type `OffsetDateTimeProvider` will be added to your application context automatically.
This provider will use the system clock to provide `OffsetDateTime` instances to the entity fields annotated with
`@LastModifiedDate` and  `@CreatedDate`.

You can choose to provide a custom clock, if you add a bean of type `Clock` and with the
name `databaseDateTimeProviderClock`

```java

@Configuration
public class CustomClockConfiguration {
    @Bean
    public Clock databaseDateTimeProviderClock() {
        return Clock.fixed(0L, ZoneOffset.UTC);
    }
}
```

The configuration above will result in all created and modified timestamps to always have the same value.

You also have the option to replace the default `OffsetDateTimeProvider` with your custom implementation. Simply
provide a bean of type `DateTimeProvider` with the name `databaseDateTimeProvider`:

```java

@Configuration
public class CustomDateTimeProviderConfiguration {
    @Bean
    public DateTimeProvider databaseDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
```

## Auditor Aware

A `AuditorAware<String>` bean will be added to your application context automatically. Depending on your applications
context and dependencies, this bean will either be of type `PrincipalNameAuditorAware` or `FixedNameAuditorAware`.

The `FixedNameAuditorAware` will be used, if no spring security is detected on your classpath.

> **NOTE**: If no spring security is on your classpath, all modified by and created by fields will always have the
> same value. If not overridden, this will have the value of the system auditor, which, by default, is a UUID consisting
> of only zeros: `00000000-0000-0000-0000-000000000000`

The `PrincipalNameAuditorAware` implementation should be the default and will be active, if spring security is detected
on your classpath. It will extract the current principals name from the `SecurityContext`. If no principal can
be found in the current context, a default system auditor will be used in favor of a null value. If not overridden,
the system auditor is a UUID consisting of zeros, only: `00000000-0000-0000-0000-000000000000`

### System Auditor

If no principal is present in the current security context when an entity is created or modified, the system auditor
will be used for the created by and modified by fields. The system auditor is a UUID consisting of only zeros:
`00000000-0000-0000-0000-000000000000`.

You have the option to override that value by setting the property `gcoding.database.auditing.system-principal` to
a string value according to your needs.

## Using @EnableJpaAuditing

This starter project enabled jpa auditing automatically for you. There is no need to add the `@EnableJpaAuditing`
manually. In fact, the application will fail to start, if you do so, because we won't be able to determine which
configuration you want to apply. If you want to ship your own auditing configuration or if you cannot remove your
custom `@EnableJpaAuditing` annotation. You might want to consider disabling the auditing functionalities of this
starter project by specifying the property `gcoding.database.auditing.enabled=false`.

## Configuration Properties

| Property                                     | Description                                                                                                                                                                                        | Default Value                          |
|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| `gcoding.database.enabled`                   | Enables or disables all database related auto configuration that was added by this starter project                                                                                                 | `true`                                 |
| `gcoding.database.auditing.enabled`          | Enable or Disable the auditing features of this starter project (created and modified dates, versioning and created and modified by fields)                                                        | `true`                                 |
| `gcoding.database.auditing.system-principal` | Specify a system principal that will be applied to created by and modified by fields in case no principal is present in the current security context at the time of creating or modifying entities | `00000000-0000-0000-0000-000000000000` |
