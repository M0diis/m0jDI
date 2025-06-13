package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.exception.InjectionException;
import me.m0dii.m0jdi.exception.MissingAnnotationException;
import me.m0dii.m0jdi.exception.MissingConstructorException;
import me.m0dii.m0jdi.exception.MultipleConstructorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class InjectorContainer {
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> componentImplementations = new HashMap<>();

    /**
     * Registers a class as a singleton or component.
     * If the class is marked with the {@link Singleton} annotation, it initializes
     * and stores an instance of the class for future use.
     * If the class is marked with the {@link Component} annotation, it is registered as a component without being stored.
     *
     * @param clazz The class to be registered as a singleton or component.
     * @param <T> The type of the class being registered.
     * @throws InjectionException If the instance creation for a singleton fails.
     */
    public <T> void registerSingleton(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                singletonInstances.put(clazz, constructor.newInstance());
            } catch (Exception e) {
                throw new InjectionException("Failed to create singleton instance for " + clazz.getName());
            }
        } else if (clazz.isAnnotationPresent(Component.class)) {
            for (Class<?> iface : clazz.getInterfaces()) {
                componentImplementations.put(iface, clazz);
            }
        } else {
            System.out.println("Warning: Trying to register non-singleton class: " + clazz.getSimpleName());
        }
    }

    /**
     * Resolves and returns an instance of the specified class.
     * <ul>
     *     <li>If the class is annotated with {@link Singleton} and already registered, the existing instance is returned.</li>
     *     <li>If the class is annotated with {@link Singleton} but not registered, a new instance is created, registered, and then returned.</li>
     *     <li>If the class is annotated with {@link Component} but not {@link Singleton}, a new instance is always created and returned.</li>
     *     <li>If the class is an interface and an implementation is registered, resolves and returns its implementation.</li>
     * </ul>
     *
     * @param clazz The class to resolve an instance for.
     * @param <T> The type of the class being resolved.
     * @return The resolved instance of the specified class.
     * @throws MissingAnnotationException If the class is not annotated with {@link Component} or {@link Singleton}.
     * @throws InjectionException If instance creation fails or the class does not have a valid constructor.
     */
    public <T> T resolve(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Component.class) && !clazz.isAnnotationPresent(Singleton.class)) {
            throw new MissingAnnotationException("Class " + clazz.getName() + " is not annotated with @Component or @Singleton.");
        }

        if (clazz.isInterface() && componentImplementations.containsKey(clazz)) {
            Class<?> implClass = componentImplementations.get(clazz);
            return clazz.cast(resolve(implClass));
        }

        if (clazz.isAnnotationPresent(Singleton.class)) {
            if (singletonInstances.containsKey(clazz)) {
                return clazz.cast(singletonInstances.get(clazz));
            }

            return resolveSingleton(clazz);
        } else {
            return resolveDependency(clazz);
        }
    }

    private <T> T resolveDependency(Class<T> clazz) {
        try {
            var annotatedConstructors = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                    .toList();

            if (annotatedConstructors.isEmpty()) {
                var defaultConstructor = clazz.getDeclaredConstructor();

                if (defaultConstructor.getModifiers() != java.lang.reflect.Modifier.PUBLIC) {
                    throw new MissingConstructorException("No public no-argument constructor found for " + clazz.getName() +
                            ". Make sure the class has a public no-argument constructor or is a static nested class.");
                }

                defaultConstructor.setAccessible(true);
                T instance = defaultConstructor.newInstance();
                singletonInstances.put(clazz, instance);
                return instance;
            }

            if (annotatedConstructors.size() > 1) {
                throw new MultipleConstructorException("Multiple constructors annotated with @Inject found for " + clazz.getName() +
                        ". Only one constructor can be annotated with @Inject.");
            }

            var constructor = annotatedConstructors.getFirst();

            constructor.setAccessible(true);
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(this::resolve)
                    .toArray();
            return (T) constructor.newInstance(params);
        }
        catch (MissingConstructorException | MultipleConstructorException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new InjectionException("No default constructor found for " + clazz.getName() +
                    ". Make sure the class has a public no-argument constructor or is a static nested class.");
        } catch (Exception e) {
            throw new InjectionException("Failed to create instance for " + clazz.getName());
        }
    }

    private <T> T resolveSingleton(Class<T> clazz) {
        try {
            var annotatedConstructors = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                    .toList();

            if (annotatedConstructors.isEmpty()) {
                var defaultConstructor = clazz.getDeclaredConstructor();
                defaultConstructor.setAccessible(true);
                T instance = defaultConstructor.newInstance();
                singletonInstances.put(clazz, instance);
                return instance;
            }

            var annotatedConstructor = annotatedConstructors.getFirst();
            annotatedConstructor.setAccessible(true);
            Object[] params = Arrays.stream(annotatedConstructor.getParameterTypes())
                    .map(this::resolve)
                    .toArray();
            T instance = (T) annotatedConstructor.newInstance(params);
            singletonInstances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new InjectionException("Failed to create singleton instance for " + clazz.getName());
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

    /**
     * Scans the specified package for classes annotated with @Component or @Singleton
     * and registers them automatically.
     *
     * @param packageName The package name to scan
     */
    public void scanPackage(String packageName) {
        try {
            List<Class<?>> classes = findClasses(packageName);

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Singleton.class)) {

                    // Register for concrete classes
                    registerSingleton(clazz);

                    // Also register the component for each interface it implements
                    for (Class<?> iface : clazz.getInterfaces()) {
                        componentImplementations.put(iface, clazz);
                    }
                }
            }
        } catch (Exception e) {
            throw new InjectionException("Failed to scan package " + packageName);
        }
    }

    private List<Class<?>> findClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    // Skip if class cannot be loaded
                }
            }
        }

        return classes;
    }

    public boolean hasImplementation(Class<?> interfaceType) {
        return componentImplementations.containsKey(interfaceType);
    }

    public Object getComponent(Class<?> type) {
        return componentImplementations.get(type);
    }

    public Set<Object> getAllComponents() {
        return Set.copyOf(componentImplementations.values());
    }
}