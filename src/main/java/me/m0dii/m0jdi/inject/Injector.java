package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.exception.InjectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Injector {
    private final InjectorContainer container;
    private final boolean autowireByType;

    public Injector(InjectorContainer container) {
        this(container, true); // Default to autowiring by type
    }

    public Injector(InjectorContainer container, boolean autowireByType) {
        this.container = container;
        this.autowireByType = autowireByType;
    }

    public static void inject(Object target) {
        if (target == null) {
            return;
        }

        Injector injector = new Injector(new InjectorContainer());
        injector.injectDependencies(target);
    }

    /**
     * Creates an instance of the specified class.
     * <p>
     * If a constructor in the class is annotated with {@link Inject}, it resolves dependencies
     * for the constructor parameters using the {@link InjectorContainer} and invokes the annotated constructor.
     * If no such constructor exists, it tries to invoke the default no-argument constructor.
     * </p>
     *
     * @param clazz The class to instantiate.
     * @param <T>   The type of the class being instantiated.
     * @return A new instance of the specified class.
     * @throws InjectionException If instantiation or dependency injection fails, or no suitable constructor is found.
     */
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> clazz) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] dependencies = Arrays.stream(parameterTypes)
                        .map(container::resolve)
                        .toArray();

                try {
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(dependencies);
                } catch (Exception e) {
                    throw new InjectionException("Failed to instantiate " + clazz + " with @Inject constructor");
                }
            }
        }

        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InjectionException("Failed to instantiate " + clazz + ". No @Inject constructor or default constructor found.");
        }
    }

    /**
     * Injects dependencies into the fields annotated with {@link Injected} in the specified target object.
     * <p>
     * The method traverses the class hierarchy of the target object. For each field annotated with
     * {@link Injected}, it resolves the dependency from the {@link InjectorContainer} and assigns it
     * to the field. If dependency injection fails at any point, an {@link InjectionException} is thrown.
     * </p>
     *
     * @param target The object whose dependencies should be injected. If {@code null}, the method does nothing.
     * @throws InjectionException If the dependency cannot be resolved or an error occurs during field injection.
     */
    public void injectDependencies(Object target) {
        if (target == null) {
            return;
        }

        Class<?> currentClass = target.getClass();
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();

            Arrays.stream(fields)
                    .filter(field -> field.isAnnotationPresent(Injected.class)
                            || (autowireByType && shouldAutowire(field.getType())))
                    .forEach(field -> {
                        Object dependency = container.resolve(field.getType());
                        if (dependency != null) {
                            injectDependencies(dependency);
                            field.setAccessible(true);
                            try {
                                field.set(target, dependency);
                            } catch (IllegalAccessException e) {
                                throw new InjectionException("Failed to inject dependency into " + field.getName());
                            }
                        }
                    });

            currentClass = currentClass.getSuperclass();
        }
    }

    private boolean shouldAutowire(Class<?> type) {
        return type.isAnnotationPresent(Component.class) ||
                type.isAnnotationPresent(Singleton.class) ||
                (type.isInterface() && container.hasImplementation(type));
    }
}