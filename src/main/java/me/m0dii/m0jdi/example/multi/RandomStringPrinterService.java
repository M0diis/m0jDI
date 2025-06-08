package me.m0dii.m0jdi.example.multi;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Injected;

@Component
public class RandomStringPrinterService {
    @Injected
    private StringProviderService stringProvider;

    public StringProviderService getStringProvider() {
        return stringProvider;
    }

    public void printRandomString() {
        System.out.println(
                "StringProviderService called from StringAccessorService! " +
                "Random string: " + stringProvider.getRandomString()
        );
    }
}
