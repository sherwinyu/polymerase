package com.joshma.polymerase.rep;

import com.joshma.polymerase.paxos.PaxosPeer;
import com.joshma.polymerase.paxos.PaxosValue;
import com.joshma.polymerase.paxos.PlayHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Given a Replicator, handles actual Paxos-style replication across them.
 */
public class ReplicationHandler implements InvocationHandler {

    private final String objectId;
    private final PaxosPeer peer;
    private final LocalReplicationStore store;

    public ReplicationHandler(String objectId, PaxosPeer peer, LocalReplicationStore store) {
        this.objectId = objectId;
        this.peer = peer;
        this.store = store;
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
            if (!(loggedValue instanceof Event)) {
                throw new RuntimeException("Unable to process non-Event type.");
            }
            Event event = (Event) loggedValue;
            if (event.method == null) {
                // noop.
                return;
            }
            // Run the event!
            String objectId = event.id;
            Method method = event.method;
            Object[] args = event.args;
            Object replicatedObject = store.get(objectId);
            try {
                method.invoke(replicatedObject, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public PaxosValue getNoop() {
            return new Event("", null, null);
        }
    }
}
