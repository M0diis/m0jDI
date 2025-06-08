package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.exception.InjectionException;
import me.m0dii.m0jdi.exception.MissingConstructorException;
import me.m0dii.m0jdi.exception.MultipleConstructorException;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Injector {
    private final InjectorContainer container;

    public Injector(InjectorContainer container) {
        this.container = container;
    }

    public static void inject(Object... targets) {
        if (targets == null) {
            return;
        }

        Injector injector = new Injector(new InjectorContainer());

        for (Object target : targets) {
            injector.injectDependencies(target);
        }
    }

    public static <T> T create(Class<T> clazz) {
        return new Injector(new InjectorContainer()).createInstance(clazz);
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
        var annotatedConstructors = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();

        if (annotatedConstructors.isEmpty()) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new MissingConstructorException("Failed to instantiate " + clazz + ". No @Inject or default constructor found.");
            }
        }

        if (annotatedConstructors.size() > 1) {
            throw new MultipleConstructorException("Multiple @Inject constructors found for " + clazz + ". Only one is allowed.");
        }

        var constructor = annotatedConstructors.getFirst();

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
                    .filter(field -> field.isAnnotationPresent(Injected.class))
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
}