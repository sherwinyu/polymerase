package com.joshma.polymerase.rep;

import com.joshma.polymerase.paxos.PaxosPeer;
import com.joshma.polymerase.paxos.PaxosValue;
import com.joshma.polymerase.paxos.PlayHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Given a Replicator, handles actual Paxos-style replication across them.
 */
public class ReplicationHandler implements InvocationHandler {

    private final PaxosPeer peer;
    private final String objectId;

    public ReplicationHandler(String objectId, PaxosPeer peer) {
        this.objectId = objectId;
        this.peer = peer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO Check that args are all serializable.

        // Log call into Paxos.
        Event event = new Event(objectId, method, args);
        int sequenceNumber = peer.log(event);
        peer.play(sequenceNumber, new ReplicationPlayHandler());

        System.err.printf("METHOD %s WAS INVOKED WITH ARGS %s\n", method, args);
        return null; // method.invoke(delegate, args);
    }

    private class ReplicationPlayHandler implements PlayHandler {
        @Override
        public void process(int sequenceNumber, PaxosValue loggedValue) {
            // TODO
        }
    }
}
