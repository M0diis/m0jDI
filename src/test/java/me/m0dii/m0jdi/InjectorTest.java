package me.m0dii.m0jdi;

import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.components.ClientWithNonSingleton;
import me.m0dii.m0jdi.components.NonSingletonService;
import me.m0dii.m0jdi.inject.Injector;
import me.m0dii.m0jdi.inject.InjectorContainer;
import me.m0dii.m0jdi.singletons.ClientWithSingleton;
import me.m0dii.m0jdi.singletons.SingletonService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {
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
        ClientWithSingleton client = new ClientWithSingleton();
        Injector.inject(client);

        assertNotNull(client.getSingletonService());
        client.getSingletonService().perform();
        assertTrue(client.getSingletonService().hasPerformed());
    }

    @Test
    void testInjectNonSingletonDependency() {
        ClientWithNonSingleton client = new ClientWithNonSingleton();

        Injector.inject(client);

        assertNotNull(client.getNonSingletonService());
        client.getNonSingletonService().execute();
        assertTrue(client.getNonSingletonService().hasExecuted());
    }

    @Test
    void testInjectNonSingletonDependencyShouldDiffer() {
        ClientWithNonSingleton client1 = new ClientWithNonSingleton();
        ClientWithNonSingleton client2 = new ClientWithNonSingleton();

        Injector.inject(client1, client2);

        assertNotNull(client1.getNonSingletonService());
        client1.getNonSingletonService().execute();
        assertTrue(client1.getNonSingletonService().hasExecuted());

        assertNotNull(client2.getNonSingletonService());
        client2.getNonSingletonService().execute();
        assertTrue(client2.getNonSingletonService().hasExecuted());

        assertNotEquals(client1.hashCode(), client2.hashCode());
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
        DerivedClient client = new DerivedClient();
        Injector.inject(client);

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
        ComplexClient client = new ComplexClient();
        Injector.inject(client);

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
        Client client = new Client();
        Injector.inject(client);

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
        private final ServiceC serviceC;
        private final ServiceD serviceD;

        @Inject
        public ComplexService(ServiceC serviceC, ServiceD serviceD) {
            this.serviceC = serviceC;
            this.serviceD = serviceD;
        }

        public String getCombinedValue() {
            return serviceC.getValue() + serviceD.getValue();
        }
    }

    @Test
    void testCreateInstanceWithMultipleParameters() {
        ComplexService service = Injector.create(ComplexService.class);

        assertNotNull(service);
        assertEquals("CD", service.getCombinedValue());
    }
}