package com.joshma.polymerase.rep;

import com.joshma.polymerase.paxos.PaxosValue;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Represents a method call.
 */
public class Event implements PaxosValue {

    private final String id;
    private final Method method;
    private final Object[] args;
    private final UUID uuid;

    public Event(String id, Method method, Object[] args) {
        this.id = id;
        this.method = method;
        this.args = args;
        this.uuid = UUID.randomUUID();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
