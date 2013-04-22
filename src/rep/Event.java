package com.joshma.polymerase.rep;

import java.lang.reflect.Method;

/**
 * Represents a method call.
 */
public class Event {

    private final Method method;
    private final Object[] args;

    public Event(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}
