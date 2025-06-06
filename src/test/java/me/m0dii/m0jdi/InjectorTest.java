package me.m0dii.m0jdi;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
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
        @Injected
        private SingletonService singletonService;

        public SingletonService getSingletonService() {
            return singletonService;
        }
    }

    static class ClientWithNonSingleton {
        @Injected
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

    @Singleton
    static class DependencyA {
        public String getValue() {
            return "A";
        }
    }

    static class ServiceWithInjectConstructor {
        private final DependencyA dependencyA;

        @Inject
        public ServiceWithInjectConstructor(DependencyA dependencyA) {
            this.dependencyA = dependencyA;
        }

        public DependencyA getDependencyA() {
            return dependencyA;
        }
    }

    @Test
    void testCreateInstanceWithInjectConstructor() {
        InjectorContainer container = new InjectorContainer();
        container.register(DependencyA.class);

        Injector injector = new Injector(container);
        ServiceWithInjectConstructor service = injector.createInstance(ServiceWithInjectConstructor.class);

        assertNotNull(service);
        assertNotNull(service.getDependencyA());
        assertEquals("A", service.getDependencyA().getValue());
    }

    @Singleton
    static class BaseService {
        public String getBaseValue() {
            return "base";
        }
    }

    static class BaseClient {
        @Injected
        protected BaseService baseService;
    }

    static class DerivedClient extends BaseClient {
        public BaseService getBaseService() {
            return baseService;
        }
    }

    @Test
    void testInheritedFieldInjection() {
        InjectorContainer container = new InjectorContainer();
        container.register(BaseService.class);

        DerivedClient client = new DerivedClient();
        Injector injector = new Injector(container);
        injector.injectDependencies(client);

        assertNotNull(client.getBaseService());
        assertEquals("base", client.getBaseService().getBaseValue());
    }

    @Singleton
    static class ServiceA {
        public String getValue() {
            return "A";
        }
    }

    @Singleton
    static class ServiceB {
        public String getValue() {
            return "B";
        }
    }

    static class ComplexClient {
        @Injected
        private ServiceA serviceA;

        @Injected
        private ServiceB serviceB;

        public ServiceA getServiceA() {
            return serviceA;
        }

        public ServiceB getServiceB() {
            return serviceB;
        }
    }


    @Test
    void testMultipleDependencyInjection() {

        InjectorContainer container = new InjectorContainer();
        container.register(ServiceA.class);
        container.register(ServiceB.class);

        ComplexClient client = new ComplexClient();
        Injector injector = new Injector(container);
        injector.injectDependencies(client);

        assertNotNull(client.getServiceA());
        assertNotNull(client.getServiceB());
        assertEquals("A", client.getServiceA().getValue());
        assertEquals("B", client.getServiceB().getValue());
    }

    @Singleton
    static class InnerDependency {
        public String getValue() {
            return "inner";
        }
    }

    @Singleton
    static class OuterDependency {
        @Injected
        private InnerDependency innerDependency;

        public InnerDependency getInnerDependency() {
            return innerDependency;
        }

        public String getValue() {
            return "outer";
        }
    }

    static class Client {
        @Injected
        private OuterDependency outerDependency;

        public OuterDependency getOuterDependency() {
            return outerDependency;
        }
    }

    @Test
    void testNestedDependencyInjection() {
        InjectorContainer container = new InjectorContainer();
        container.register(InnerDependency.class);
        container.register(OuterDependency.class);

        Client client = new Client();
        Injector injector = new Injector(container);
        injector.injectDependencies(client);
        injector.injectDependencies(client.getOuterDependency()); // Inject into nested dependency

        assertNotNull(client.getOuterDependency());
        assertNotNull(client.getOuterDependency().getInnerDependency());
        assertEquals("outer", client.getOuterDependency().getValue());
        assertEquals("inner", client.getOuterDependency().getInnerDependency().getValue());
    }

    @Singleton
    static class ServiceC {
        public String getValue() {
            return "C";
        }
    }

    @Singleton
    static class ServiceD {
        public String getValue() {
            return "D";
        }
    }

    static class ComplexService {
        private final ServiceC serviceA;
        private final ServiceD serviceB;

        @Inject
        public ComplexService(ServiceC serviceA, ServiceD serviceB) {
            this.serviceA = serviceA;
            this.serviceB = serviceB;
        }

        public String getCombinedValue() {
            return serviceA.getValue() + serviceB.getValue();
        }
    }

    @Test
    void testCreateInstanceWithMultipleParameters() {
        InjectorContainer container = new InjectorContainer();
        container.register(ServiceC.class);
        container.register(ServiceD.class);

        Injector injector = new Injector(container);
        ComplexService service = injector.createInstance(ComplexService.class);

        assertNotNull(service);
        assertEquals("CD", service.getCombinedValue());
    }
}