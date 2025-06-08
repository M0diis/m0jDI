package me.m0dii.m0jdi.components;

import me.m0dii.m0jdi.annotations.Component;

@Component
public class NonSingletonService {
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
