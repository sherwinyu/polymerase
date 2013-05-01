package com.joshma.polymerase.rep;

import com.joshma.polymerase.paxos.PaxosPeer;
import com.joshma.polymerase.paxos.PaxosValue;
import com.joshma.polymerase.paxos.PlayHandler;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Given a Replicator, handles actual Paxos-style replication across them.
 */
public class ReplicationHandler implements InvocationHandler {

    private final int me;
    private final String objectId;
    private final PaxosPeer peer;
    private final LocalReplicationStore store;

    public ReplicationHandler(int me, String objectId, PaxosPeer peer, LocalReplicationStore store) {
        this.me = me;
        this.objectId = objectId;
        this.peer = peer;
        this.store = store;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (Object arg : args) {
                if (!(Serializable.class.isAssignableFrom(arg.getClass()))) {
                    throw new RuntimeException(String.format("Could not call method %s! All arguments must implement Serializable - %s does not.",
                            method.getName(), arg.getClass().getName()));
                }
            }
        }

        // Log call into Paxos.
        Event event = new Event(objectId, method, args);
        int sequenceNumber = peer.log(event);

        System.err.printf("[%d] METHOD %s BEING INVOKED WITH ARGS %s\n", me, method, args);

        return peer.play(sequenceNumber, new ReplicationPlayHandler());
    }

    private class ReplicationPlayHandler implements PlayHandler {
        @Override
        public Object process(int sequenceNumber, PaxosValue loggedValue) {
            System.err.printf("[%d] Processing seq=%d, value=%s\n", me, sequenceNumber, loggedValue);
            if (!(loggedValue instanceof Event)) {
                throw new RuntimeException("Unable to process non-Event type.");
            }
            Event event = (Event) loggedValue;
            if (event.getMethod() == null) {
                // noop.
                return null;
            }
            // Run the event!
            String objectId = event.id;
            Method method = event.getMethod();
            Object[] args = event.args;
            Object replicatedObject = store.get(objectId);
            System.err.printf("[%d] RUNNING seq=%s, %s => %s ON %s\n", me, sequenceNumber, replicatedObject, method, args);
            try {
                return method.invoke(replicatedObject, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public PaxosValue getNoop() {
            return new Event("", null, null);
        }
    }
}
