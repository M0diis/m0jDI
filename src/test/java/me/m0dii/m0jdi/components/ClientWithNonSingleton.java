package me.m0dii.m0jdi.components;

import me.m0dii.m0jdi.annotations.Injected;

public class ClientWithNonSingleton {
    @Injected
    private NonSingletonService nonSingletonService;

    public NonSingletonService getNonSingletonService() {
        return nonSingletonService;
    }
}
