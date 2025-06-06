package me.m0dii.m0jdi.inject;

import me.m0dii.m0jdi.annotations.Inject;

import java.lang.reflect.Field;

public class Injector {
    private final InjectorContainer container;

    public Injector(InjectorContainer container) {
        this.container = container;
    }

    public void injectDependencies(Object target) {
        if (target == null) {
            return;
        }

        Class<?> currentClass = target.getClass();
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = container.resolve(field.getType());
                    if (dependency != null) {
                        field.setAccessible(true);
                        try {
                            field.set(target, dependency);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to inject dependency", e);
                        }
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}