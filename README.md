# utilities-spring-boot

[![Release](https://github.com/gregor-rieble/utilities-spring-boot/actions/workflows/release.yml/badge.svg)](https://github.com/gregor-rieble/utilities-spring-boot/actions/workflows/release.yml)
[![Build & Deploy SNAPSHOT](https://github.com/gregor-rieble/utilities-spring-boot/actions/workflows/deploy-snapshot.yml/badge.svg)](https://github.com/gregor-rieble/utilities-spring-boot/actions/workflows/deploy-snapshot.yml)

## Introduction

This project provides custom spring boot starter projects to simplify bootstrapping microservice applications with
additional features such as emitting events through aspects

## Usage

To opt-in for all of the additional starters and features, add the following dependency to your maven pom file:

```xml

<dependency>
    <groupId>de.gcoding.boot</groupId>
    <artifactId>utilities-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

Alternatively, you can select only the features & starters that you want. See the readme of the desired
starter project for information on how to use them.

## Available starters

| starter                                        | description                                                                                                                                                                                    |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [business-events](./business-events/README.md) | Provides the `@EmitBusinessEvent` and `@BusinessEventListener` annotations to emit and listen for business events using an annotation (aspect) based approach                                  |
| [database](./database/README.md)               | Contains a base entity that is ready to be extended for auditing purposes. Also includes custom expressions that can be used with specifications.                                              |
| [diagnostics](./diagnostics/README.md)         | Adds options to easily let users of spring boot applications know why the application failed to start by using convenience exceptions & interfaces that play into the spring `FailureAnalyzer` |
