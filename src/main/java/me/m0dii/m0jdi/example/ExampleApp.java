package me.m0dii.m0jdi.example;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.inject.Injector;
import me.m0dii.m0jdi.inject.InjectorContainer;

public class ExampleApp {

    static class Service {
        private static int idCounter = 0;
        private final int id;

        public Service() {
            this.id = ++idCounter;
        }

        @Override
        public String toString() {
            return "Service ID: " + id;
        }
    }

    @Singleton
    static class SingletonService {
        private static int idCounter = 0;
        private final int id;

        public SingletonService() {
            this.id = ++idCounter;
        }

        @Override
        public String toString() {
            return "SingletonService ID: " + id;
        }
    }

    static class Target {
        private final Service service;
        private final SingletonService singletonService;

        @Injected
        private Service anotherService;
        @Injected
        private SingletonService anotherSingletonService;

        @Inject
        public Target(Service service, SingletonService singletonService) {
            this.service = service;
            this.singletonService = singletonService;
        }

        @Override
        public String toString() {
            return "Target{" +
                    "service=" + service +
                    ", singletonService=" + singletonService +
                    ", anotherService=" + anotherService +
                    ", anotherSingletonService=" + anotherSingletonService +
                    '}';
        }
    }

    public void run() {
        InjectorContainer container = new InjectorContainer();

        // Register singleton classes explicitly
        container.register(SingletonService.class);

        Injector injector = new Injector(container);

        Target target = injector.createInstance(Target.class);
        injector.injectDependencies(target);

        System.out.println("Created target: " + target);

        // Create another target to demonstrate singleton vs non-singleton behavior
        Target target2 = injector.createInstance(Target.class);
        injector.injectDependencies(target2);

        System.out.println("Created target2: " + target2);
    }

    public static void main(String[] args) {
        var app = new ExampleApp();
        app.run();
    }
}