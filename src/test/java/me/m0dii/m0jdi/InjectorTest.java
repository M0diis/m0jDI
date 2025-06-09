package me.m0dii.m0jdi;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.annotations.Singleton;
import me.m0dii.m0jdi.components.ClientWithNonSingleton;
import me.m0dii.m0jdi.components.NonSingletonService;
import me.m0dii.m0jdi.exception.MissingAnnotationException;
import me.m0dii.m0jdi.exception.MissingConstructorException;
import me.m0dii.m0jdi.exception.MultipleConstructorException;
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
        container.registerSingleton(SingletonService.class);

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
    static class ServiceWithInjectConstructorDependency {
        public String getValue() {
            return "Inject Constructor Dependency A";
        }
    }

    static class ServiceWithInjectConstructor {
        private final ServiceWithInjectConstructorDependency serviceWithInjectConstructorDependency;

        @Inject
        public ServiceWithInjectConstructor(ServiceWithInjectConstructorDependency serviceWithInjectConstructorDependency) {
            this.serviceWithInjectConstructorDependency = serviceWithInjectConstructorDependency;
        }

        public ServiceWithInjectConstructorDependency getDependencyA() {
            return serviceWithInjectConstructorDependency;
        }
    }

    @Test
    void testCreateInstanceWithInjectConstructor() {
        InjectorContainer container = new InjectorContainer();
        container.registerSingleton(ServiceWithInjectConstructorDependency.class);

        Injector injector = new Injector(container);
        ServiceWithInjectConstructor service = injector.createInstance(ServiceWithInjectConstructor.class);

        assertNotNull(service);
        assertNotNull(service.getDependencyA());
        assertEquals("Inject Constructor Dependency A", service.getDependencyA().getValue());
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
    static class ComplexClientSingletonA {
        public String getValue() {
            return "A";
        }
    }

    @Singleton
    static class ComplexClientSingletonB {
        public String getValue() {
            return "B";
        }
    }

    static class ComplexClient {
        @Injected
        private ComplexClientSingletonA complexClientSingletonA;

        @Injected
        private ComplexClientSingletonB complexClientSingletonB;

        public ComplexClientSingletonA getServiceA() {
            return complexClientSingletonA;
        }

        public ComplexClientSingletonB getServiceB() {
            return complexClientSingletonB;
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
    static class ClientInnerSingletonDependency {
        public String getValue() {
            return "inner";
        }
    }

    @Singleton
    static class ClientOuterSingletonDependency {
        @Injected
        private ClientInnerSingletonDependency clientInnerSingletonDependency;

        public ClientInnerSingletonDependency getInnerDependency() {
            return clientInnerSingletonDependency;
        }

        public String getValue() {
            return "outer";
        }
    }

    static class Client {
        @Injected
        private ClientOuterSingletonDependency clientOuterSingletonDependency;

        public ClientOuterSingletonDependency getOuterDependency() {
            return clientOuterSingletonDependency;
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
    static class ComplexServiceSingletonC {
        public String getValue() {
            return "C";
        }
    }

    @Singleton
    static class ComplexServiceSingletonD {
        public String getValue() {
            return "D";
        }
    }

    static class ComplexService {
        private final ComplexServiceSingletonC complexServiceSingletonC;
        private final ComplexServiceSingletonD complexServiceSingletonD;

        @Inject
        public ComplexService(ComplexServiceSingletonC complexServiceSingletonC, ComplexServiceSingletonD complexServiceSingletonD) {
            this.complexServiceSingletonC = complexServiceSingletonC;
            this.complexServiceSingletonD = complexServiceSingletonD;
        }

        public String getCombinedValue() {
            return complexServiceSingletonC.getValue() + complexServiceSingletonD.getValue();
        }
    }

    @Test
    void testCreateInstanceWithMultipleParameters() {
        ComplexService service = Injector.create(ComplexService.class);

        assertNotNull(service);
        assertEquals("CD", service.getCombinedValue());
    }

    @Component
    static class NoConstructorClass {
        private NoConstructorClass() {
        }
    }

    @Test
    void shouldThrowExceptionIfNoConstructorFound() {
        InjectorContainer container = new InjectorContainer();
        container.registerSingleton(NoConstructorClass.class);

        Exception exception = assertThrows(MissingConstructorException.class, () -> {
            container.resolve(NoConstructorClass.class);
        });

        String expectedMessage = "No public no-argument constructor found for me.m0dii.m0jdi.InjectorTest$NoConstructorClass. " +
                "Make sure the class has a public no-argument constructor or is a static nested class.";

        assertEquals(expectedMessage, exception.getMessage());
    }

    static class NoAnnotationClass {

    }

    @Test
    void shouldThrowExceptionIfNoAnnotationFound() {
        InjectorContainer container = new InjectorContainer();
        container.registerSingleton(NoConstructorClass.class);

        Exception exception = assertThrows(MissingAnnotationException.class, () -> {
            container.resolve(NoAnnotationClass.class);
        });

        String expectedMessage = "Class me.m0dii.m0jdi.InjectorTest$NoAnnotationClass is not annotated with @Component or @Singleton.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Component
    static class MultipleConstructorsClass {
        @Inject
        public MultipleConstructorsClass(String value) {
        }

        @Inject
        public MultipleConstructorsClass(int value) {
        }
    }

    @Test
    void shouldThrowExceptionIfMultipleConstructorsFound() {
        InjectorContainer container = new InjectorContainer();
        container.registerSingleton(MultipleConstructorsClass.class);

        Exception exception = assertThrows(MultipleConstructorException.class, () -> {
            container.resolve(MultipleConstructorsClass.class);
        });

        String expectedMessage = "Multiple constructors annotated with @Inject found for me.m0dii.m0jdi.InjectorTest$MultipleConstructorsClass. " +
                "Only one constructor can be annotated with @Inject.";
        assertEquals(expectedMessage, exception.getMessage());
    }
}