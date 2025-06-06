# m0jDI - Java Dependency Injection Library

m0jDI a very simple, lightweight dependency injection library for Java, designed to simplify object creation and
dependency
management. It supports automatic handling of singletons and non-singleton classes using annotations.

## Features

- **Singleton Support**: Automatically manage singleton instances using the `@Singleton` annotation.
- **Constructor Injection**: Inject dependencies via constructors annotated with `@Inject`.
- **Field Injection**: Inject dependencies into fields annotated with `@Inject`.

## Installation

Add the library to your project using Gradle:

```gradle
dependencies {
    implementation 'me.m0dii:m0jdi:1.0.0'
}
```

## Usage

1. **Mark Classes with Annotations**

Use the `@Singleton` annotation to mark classes as singletons. Use the `@Inject` annotation for fields or
constructors that require dependency injection.

Example:

```java

@Singleton
public class Service {
    public void perform() {
        System.out.println("Service performed!");
    }
}
```

2. **Set up Your Application**

Create an InjectorContainer, register your classes, and use Injector to inject dependencies.

Example:

```java
public class App {
    public static void main(String[] args) {
        InjectorContainer container = new InjectorContainer();
        container.register(Service.class); // Register your classes

        Service service = container.resolve(Service.class); // Resolve instance
        Injector injector = new Injector(container);
        injector.injectDependencies(service); // Inject dependencies if needed

        service.perform();
    }
}
```

3. **Constructor Injection**

Annotate the constructor with `@Inject` to automatically inject its dependencies. (Note: You must resolve the class via
the container for constructor injection to work.)

Example:

```java
public class Controller {
    private final Service service;

    @Inject
    public Controller(Service service) {
        this.service = service;
    }

    public void handleRequest() {
        service.perform();
    }
}

// Usage:
// container.register(Controller.class);
// Controller controller = container.resolve(Controller.class);
// injector.injectDependencies(controller);
```

4. **Field Injection**

Use the `@Inject` annotation on fields to inject dependencies. After resolving the instance, call
`injector.injectDependencies`.

Example:

```java
public class UserHandler {
    @Inject
    private Service service;

    public void processUser() {
        service.perform();
    }
}

// Usage:
// container.register(UserHandler.class);
// UserHandler userHandler = container.resolve(UserHandler.class);
// injector.injectDependencies(userHandler);
```
