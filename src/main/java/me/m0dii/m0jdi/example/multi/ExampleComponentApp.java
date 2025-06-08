package me.m0dii.m0jdi.example.multi;

import me.m0dii.m0jdi.annotations.Injected;
import me.m0dii.m0jdi.inject.Injector;

public class ExampleComponentApp {

    @Injected
    private RandomStringPrinterService randomStringPrinterService;

    @Injected
    private StringProviderService stringProviderService;

    @Injected
    private RandomStringSingleton randomStringSingleton;

    public void run() {
        Injector.inject(this);

        randomStringPrinterService.printRandomString();

        // The RandomStringSingleton should be the same instance because it's a singleton
        var fromPrinter = randomStringPrinterService.getStringProvider().getRandomStringService();
        var fromApp = randomStringSingleton;

        System.out.println("RandomStringSingleton from printer: " + fromPrinter.hashCode());
        System.out.println("RandomStringSingleton from app: " + fromPrinter.hashCode());
        System.out.println("Are both instances the same? " + (fromPrinter == fromApp));

        // StringProviderService should be different instances since it's not a singleton
        var fromPrinterService = randomStringPrinterService.getStringProvider();
        var fromAppService = stringProviderService;

        System.out.println("StringProviderService from printer: " + fromPrinterService.hashCode());
        System.out.println("StringProviderService from app: " + fromAppService.hashCode());
        System.out.println("Are both StringProviderService instances the same? " + (fromPrinterService == fromAppService));
    }

    public static void main(String[] args) {
        var app = new ExampleComponentApp();
        app.run();
    }
}