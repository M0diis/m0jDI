package me.m0dii.m0jdi.singletons;

import me.m0dii.m0jdi.annotations.Injected;

public class ClientWithSingleton {
    @Injected
    private SingletonService singletonService;

    public SingletonService getSingletonService() {
        return singletonService;
    }
}
