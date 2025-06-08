package me.m0dii.m0jdi.singletons;

import me.m0dii.m0jdi.annotations.Singleton;

@Singleton
public class SingletonService {
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
