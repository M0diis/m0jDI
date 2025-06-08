package me.m0dii.m0jdi.example.multi;

import me.m0dii.m0jdi.annotations.Component;
import me.m0dii.m0jdi.annotations.Inject;

@Component
public class StringProviderService {
    private final RandomStringSingleton randomStringService;

    @Inject
    public StringProviderService(RandomStringSingleton randomStringService) {
        this.randomStringService = randomStringService;
    }

    public String getRandomString() {
        return randomStringService.getRandomString();
    }

    public RandomStringSingleton getRandomStringService() {
        return randomStringService;
    }
}
