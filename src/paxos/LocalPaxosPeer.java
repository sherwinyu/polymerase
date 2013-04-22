package com.joshma.polymerase.paxos;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.joshma.polymerase.Event;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class LocalPaxosPeer implements PaxosPeer {

    private final List<PaxosPeer> peers;
    private final Map<Integer, PaxosInstance> instances;
    private final int me;

    public LocalPaxosPeer(int me) {
        this.me = me;
        peers = Lists.newArrayList();
        instances = Maps.newHashMap();
    }

    public void initialize(List<PaxosPeer> peers) throws RemoteException {
        // Add all peers, including yourself.
        peers.clear();
        peers.addAll(peers);
    }

    public void start(int sequenceNumber, Event event) {
        final PaxosInstance instance = new PaxosInstance(sequenceNumber, event);
        instances.put(sequenceNumber, instance);
        new PaxosThread(instance, peers, me).start();
    }

    /**
     * Checks whether an instance has completed.
     * @param sequenceNumber
     * @return event if completed, null otherwise.
     */
    public Event status(int sequenceNumber) {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            return null;
        }
        return instance.getValue();
    }

    @Override
    public PrepareResponse prepare(int sequenceNumber, int n) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            return null;
        }
        return instance.prepare(n);
    }

    @Override
    public AcceptResponse accept(int sequenceNumber, int n, Event event) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            return null;
        }
        return instance.accept(n, event);
    }

    @Override
    public void decide(int sequenceNumber, Event event) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance != null) {
            instance.decide(event);
        }
    }
}
