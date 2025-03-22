# diagnostics

<!-- @formatter:off -->
<!-- TOC -->
* [diagnostics](#diagnostics)
  * [Introduction](#introduction)
  * [Getting Started](#getting-started)
    * [Suggest multiple Actions](#suggest-multiple-actions)
    * [Using your own Exception](#using-your-own-exception)
<!-- TOC -->
<!-- @formatter:on -->

## Introduction

Adds a `DiagnosableFailureAnalyzer` to your spring boot application that can analyze application startup exceptions that
either implement the `DiagnosisDetailsProvider` interface or are annotated with `@Diagnosable`. This allows
you to more easily give the user of your application helpful tips on how to solve startup errors without the
need to implement your own `FailureAnalyzer`.

## Getting Started

Include the starter project in one of your spring boot applications to get started:

```xml

<dependency>
    <groupId>de.gcoding.boot</groupId>
    <artifactId>diagnostics-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

During application startup, throw a `DiagnosableException` to give your users a clear indication on why the
application start failed:

```java
import java.util.Locale;

@Configuration
public class CustomConfiguration {
    @PostConstruct
    void validateConfiguration() {
        final var locale = Locale.getDefault();
        if (!locale.equals(Locale.US)) {
            throw new DiagnosableException(
                    "The application could not be started, because it must be run with en-US as it's default locale",
                    "Please specify the locale to be used for the JVM explicitly by using the JVM "
                            + "args \"-Duser.language=en -Duser.region=US\"\nor programmatically with "
                            + "Locale.setDefault(Locale.US) before the application start (e.g., in your main method)."
            );
        }
    }
}
```

If the default locale is not set to `en-US`, the spring boot application will print the following error on startup:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

The application could not be started, because it must be run with en-US as it's default locale

Action:

Please specify the locale to be used for the JVM explicitly by using the JVM args "-Duser.language=en -Duser.region=US" 
or programmatically with Locale.setDefault(Locale.US) before the application start (e.g., in your main method).
```

### Suggest multiple Actions

If your suggested action contains multiple options to solve the startup error (as with the example above), you
can make the output even more user-friendly with the help of `DiagnosisDetails`:

<!-- @formatter:off -->
```java
import java.util.Locale;

@Configuration
public class CustomConfiguration {
    @PostConstruct
    void validateConfiguration() {
        final var locale = Locale.getDefault();
        if (!locale.equals(Locale.US)) {
            throw new DiagnosableException(
                DiagnosisDetails
                    .withDescription("The application could not be started, because it must be run with en-US as it's default locale")
                    .andSuggestedActions()
                        .of("Use the JVM args \"-Duser.language=en -Duser.region=US\"")
                        .and("Use Locale.setDefault(Locale.US) before the application start (e.g., in your main method)")
            );
        }
    }
}
```
<!-- @formatter:on -->

Will now result in

```
***************************
APPLICATION FAILED TO START
***************************

Description:

The application could not be started, because it must be run with en-US as it's default locale

Action:

You can take the following actions to solve the issue:
    - Use the JVM args "-Duser.language=en -Duser.region=US"
    - Use Locale.setDefault(Locale.US) before the application start (e.g., in your main method)
```

### Using your own Exception

You can also use your own exception classes to make the application recognize it as a startup exception
by either implementing the `DiagnosableDetailsProvider` interface or by annotating your exception
with `@Diagnosable`

```java
public class CustomException implements DiagnosableDetailsProvider {
    @Override
    public DiagnosisDetails getDiagnosisDetails() {
        return new DiagnosisDetails(
                "Your application could not be started",
                "Make sure to use the correct Java version"
        );
    }
}
```

If `CustomException` is thrown during application start, spring will print the following:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Your application could not be started

Action:

Make sure to use the correct Java version
```

Alternatively, using the `@Diagnosable` annotation to achieve the same result as above:

```java

@Diagnosable(description = "Your application could not be started", action = "Make sure to use the correct Java version")
public class CustomException {
}
```
