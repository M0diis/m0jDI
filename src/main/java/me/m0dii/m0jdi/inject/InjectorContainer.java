package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Singleton;

import java.util.HashMap;
import java.util.Map;

public class InjectorContainer {
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public <T> void register(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            try {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                instances.put(clazz, constructor.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create singleton instance for " + clazz.getName(), e);
            }
        }
    }

    public <T> T resolve(Class<T> clazz) {
        if (instances.containsKey(clazz)) {
            return clazz.cast(instances.get(clazz));
        }
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            if (clazz.isAnnotationPresent(Singleton.class)) {
                instances.put(clazz, instance);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for " + clazz.getName(), e);
        }
    }
}
