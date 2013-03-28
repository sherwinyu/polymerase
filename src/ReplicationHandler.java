package com.joshma.polymerase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Given a Replicator, handles actual Paxos-style replication across them.
 */
public class ReplicationHandler implements InvocationHandler {

    private final Object delegate;

    public ReplicationHandler(Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO Check that args are all serializable.
        return method.invoke(delegate, args);
    }
}
