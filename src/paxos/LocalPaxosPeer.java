package com.joshma.polymerase.paxos;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        this.peers.clear();
        this.peers.addAll(peers);
    }

    public void start(int sequenceNumber, PaxosValue value) {
        final PaxosInstance instance = new PaxosInstance(sequenceNumber, value);
        instances.put(sequenceNumber, instance);
        new PaxosThread(instance, peers, me).start();
    }

    /**
     * Checks whether an instance has completed.
     * @param sequenceNumber
     * @return value if completed, null otherwise.
     */
    public PaxosValue status(int sequenceNumber) {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            return null;
        }
        return instance.getDecidedValue();
    }

    @Override
    public PrepareResponse prepare(int sequenceNumber, int n) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            instance = new PaxosInstance(sequenceNumber);
            instances.put(sequenceNumber, instance);
        }
        return instance.prepare(n);
    }

    @Override
    public AcceptResponse accept(int sequenceNumber, int n, PaxosValue value) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance == null) {
            return null;
        }
        return instance.accept(n, value);
    }

    @Override
    public void decide(int sequenceNumber, PaxosValue value) throws RemoteException {
        PaxosInstance instance = instances.get(sequenceNumber);
        if (instance != null) {
            instance.decide(value);
        }
    }

    @Override
    public String toString() {
        return String.format("Peer[%d]", me);
    }
}
