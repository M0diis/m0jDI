package me.m0dii.m0jdi.example.singleton;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.inject.Injector;
import me.m0dii.m0jdi.inject.InjectorContainer;

public class ExampleApp {
    static class Target {
        private final ComponentService componentService;
        private final SingletonService singletonService;

        @Injected // This is necessary to ensure that the dependencies are injected in the field
        private ComponentService anotherComponentService;
        @Injected
        private SingletonService anotherSingletonService;

        @Inject // This is necessary to ensure that the dependencies are injected in the constructor
        public Target(ComponentService componentService, SingletonService singletonService) {
            this.componentService = componentService;
            this.singletonService = singletonService;
        }

        @Override
        public String toString() {
            return "Target{" +
                    "service=" + componentService.hashCode() +
                    ", singletonService=" + singletonService.hashCode() +
                    ", anotherService=" + anotherComponentService.hashCode() +
                    ", anotherSingletonService=" + anotherSingletonService.hashCode() +
                    '}';
        }
    }

    public void run() {
        InjectorContainer container = new InjectorContainer();

        Injector injector = new Injector(container);

        Target target = injector.createInstance(Target.class);
        injector.injectDependencies(target);

        System.out.println("Target: " + target);

        // Create another target to demonstrate singleton vs non-singleton behavior
        Target target2 = injector.createInstance(Target.class);
        injector.injectDependencies(target2);

        System.out.println("Target2: " + target2);
    }

    public static void main(String[] args) {
        var app = new ExampleApp();
        app.run();
    }
}