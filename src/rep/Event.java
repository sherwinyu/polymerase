package com.joshma.polymerase.rep;

import com.joshma.polymerase.paxos.PaxosValue;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Represents a method call.
 */
public class Event implements PaxosValue {

    protected final String id;
    private final Class<?> methodClass;
    private final String methodName;
    private final Class<?>[] paramTypes;
    protected final Object[] args;
    private final UUID uuid;

    public Event(String id, Method method, Object[] args) {
        this.id = id;
        if (method == null) {
            this.methodClass = null;
            this.methodName = null;
            this.paramTypes = null;
        } else {
            this.methodClass = method.getDeclaringClass();
            this.methodName = method.getName();
            this.paramTypes = method.getParameterTypes();
        }
        this.args = args;
        this.uuid = UUID.randomUUID();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public Method getMethod() {
        if (methodClass == null) {
            return null;
        }
        try {
            return methodClass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Logged method should exist!", e);
        }
    }

    @Override
    public String toString() {
        if (methodClass == null) {
            return "Event[NOOP]";
        }
        return String.format("Event[id=%s, method=%s]", id, methodName);
    }

}
