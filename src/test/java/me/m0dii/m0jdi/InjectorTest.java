package me.m0dii.m0jdi;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.inject.Injector;
import me.m0dii.m0jdi.inject.InjectorContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {
    @Singleton
    static class SingletonService {
        private boolean performed = false;

        public void perform() {
            performed = true;
        }

        public boolean hasPerformed() {
            return performed;
        }

        public String getValue() {
            return "singleton";
        }
    }

    static class NonSingletonService {
        private boolean executed = false;

        public void execute() {
            executed = true;
        }

        public boolean hasExecuted() {
            return executed;
        }

        public String getValue() {
            return "non-singleton";
        }
    }

    static class ClientWithSingleton {
        @Inject
        private SingletonService singletonService;

        public SingletonService getSingletonService() {
            return singletonService;
        }
    }

    static class ClientWithNonSingleton {
        @Inject
        private NonSingletonService nonSingletonService;

        public NonSingletonService getNonSingletonService() {
            return nonSingletonService;
        }
    }

    @Test
    void testSingletonRegistrationAndReuse() {
        InjectorContainer container = new InjectorContainer();
        container.register(SingletonService.class);

        SingletonService service1 = container.resolve(SingletonService.class);
        SingletonService service2 = container.resolve(SingletonService.class);

        assertNotNull(service1);
        assertNotNull(service2);
        assertSame(service1, service2);
        assertEquals("singleton", service1.getValue());
        assertEquals("singleton", service2.getValue());
    }

    @Test
    void testNonSingletonRegistration() {
        InjectorContainer container = new InjectorContainer();

        NonSingletonService service1 = container.resolve(NonSingletonService.class);
        NonSingletonService service2 = container.resolve(NonSingletonService.class);

        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2);
        assertEquals("non-singleton", service1.getValue());
        assertEquals("non-singleton", service2.getValue());
    }

    @Test
    void testInjectSingletonDependency() {
        InjectorContainer container = new InjectorContainer();
        container.register(SingletonService.class);

        ClientWithSingleton client = new ClientWithSingleton();
        Injector injector = new Injector(container);
        injector.injectDependencies(client);

        assertNotNull(client.getSingletonService());
        client.getSingletonService().perform();
        assertTrue(client.getSingletonService().hasPerformed());
    }

    @Test
    void testInjectNonSingletonDependency() {
        InjectorContainer container = new InjectorContainer();

        ClientWithNonSingleton client = new ClientWithNonSingleton();
        Injector injector = new Injector(container);
        injector.injectDependencies(client);

        assertNotNull(client.getNonSingletonService());
        client.getNonSingletonService().execute();
        assertTrue(client.getNonSingletonService().hasExecuted());
    }
}