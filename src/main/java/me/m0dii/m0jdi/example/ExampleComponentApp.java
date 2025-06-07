package me.m0dii.m0jdi.example;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Inject;
import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.inject.Injector;

import java.util.Random;

public class ExampleComponentApp {

    public static final Random RANDOM = new Random();

    static class RandomStringService {
        public String getRandomString() {
            return "RandomString-" + RANDOM.nextInt(1000);
        }
    }

    static class StringProvider {
        private final RandomStringService randomStringService;

        @Inject
        public StringProvider(RandomStringService randomStringService) {
            this.randomStringService = randomStringService;
        }

        public String getRandomString() {
            return randomStringService.getRandomString() + " (from StringProvider)";
        }
    }

    @Component
    static class Printer {
        @Injected
        private StringProvider stringProvider;

        public void sayHello() {
            System.out.println("StringProvider from Printer! My random string is: " + stringProvider.getRandomString());
        }
    }

    private Printer printer;

    public void run() {
        Injector.inject(this);

        printer.sayHello();
    }

    public static void main(String[] args) {
        var app = new ExampleComponentApp();
        app.run();
    }
}