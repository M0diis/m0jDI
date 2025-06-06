package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.exception.InjectionException;

import java.util.HashMap;
import java.util.Map;

public class InjectorContainer {
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();

    /**
     * Registers a class as a singleton.
     * If the class is marked with the {@link Singleton} annotation, it initializes
     * an instance of the class and stores it for future retrieval.
     *
     * @param clazz The class to be registered as a singleton.
     * @param <T> The type of the class being registered.
     * @throws InjectionException If the singleton instance creation fails.
     */
    public <T> void register(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                singletonInstances.put(clazz, constructor.newInstance());
            } catch (Exception e) {
                throw new InjectionException("Failed to create singleton instance for " + clazz.getName());
            }
        } else {
            System.out.println("Warning: Trying to register non-singleton class: " + clazz.getSimpleName());
        }
    }

    /**
     * Resolves and returns an instance of the specified class.
     * <ul>
     *     <li>If the class is annotated with {@link Singleton} and already registered, the existing instance is returned.</li>
     *     <li>If the class is annotated with {@link Singleton} but not registered, a new instance is created and registered.</li>
     *     <li>If the class is not annotated with {@link Singleton}, a new instance is always created.</li>
     * </ul>
     *
     * @param clazz The class to resolve an instance for.
     * @param <T> The type of the class being resolved.
     * @return The resolved instance of the specified class.
     * @throws InjectionException If instance creation fails or the class does not have a no-argument constructor.
     */
    public <T> T resolve(Class<T> clazz) {
        // Check if it's a singleton
        if (clazz.isAnnotationPresent(Singleton.class)) {
            // If already registered, return existing instance
            if (singletonInstances.containsKey(clazz)) {
                return clazz.cast(singletonInstances.get(clazz));
            }

            // If not registered, create and register it
            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                T instance = constructor.newInstance();
                singletonInstances.put(clazz, instance);
                return instance;
            } catch (Exception e) {
                throw new InjectionException("Failed to create singleton instance for " + clazz.getName());
            }
        } else {
            // Not a singleton - always create new instance
            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new InjectionException("No default constructor found for " + clazz.getName() +
                        ". Make sure the class has a public no-argument constructor or is a static nested class.");
            } catch (Exception e) {
                throw new InjectionException("Failed to create instance for " + clazz.getName());
            }
        }
    }

    /**
     * Checks if a class is registered as a singleton.
     *
     * @param clazz The class to check for singleton registration.
     * @return {@code true} if the class is registered as a singleton, {@code false} otherwise.
     */
    public boolean isSingletonRegistered(Class<?> clazz) {
        return singletonInstances.containsKey(clazz);
    }
}