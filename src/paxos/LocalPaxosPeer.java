package com.joshma.polymerase.paxos;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LocalPaxosPeer implements PaxosPeer {

    private final List<PaxosPeer> peers;
    private final Map<Integer, PaxosInstance> instances;
    private final int me;
    private int curSeq;

    public LocalPaxosPeer(int me) {
        this.me = me;
        peers = Lists.newArrayList();
        instances = Maps.newHashMap();
        curSeq = -1;
    }

    public void initialize(List<PaxosPeer> peers) throws RemoteException {
        // Add all peers, including yourself.
        this.peers.clear();
        this.peers.addAll(peers);
    }

    protected void start(int sequenceNumber, PaxosValue value) {
        assert peers.size() > 0 : "Peers are empty!";
        new PaxosThread(sequenceNumber, value, peers, me).start();
    }

    public int log(PaxosValue value) {
        assert(value != null);
        // Try sequenceNumbers until we are able to log it.
        while (true) {
            // Pick a sequenceNumber.
            int sequenceNumber;
            if (instances.isEmpty()) {
                sequenceNumber = 1;
            } else {
                sequenceNumber = Collections.max(instances.keySet()) + 1;
            }

            start(sequenceNumber, value);

            PaxosValue loggedValue = readLog(sequenceNumber);
            // Our object was logged!
            if (loggedValue.getUUID().equals(value.getUUID())) {
                return sequenceNumber;
            }
        }
    }

    private PaxosValue readLog(int sequenceNumber) {
        int timeout = 10;
        while (true) {
            PaxosValue value = status(sequenceNumber);
            if (value != null) {
                return value;
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return null;
            }
            if (timeout < 10000) {
                timeout *= 2;
            }
        }
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
    public Object play(int sequenceNumber, PlayHandler handler) {
        // Play forward towards sequenceNumber.
        Object outputValue = null;
        while (curSeq <= sequenceNumber) {
            PaxosValue noop = handler.getNoop();
            start(curSeq, noop);
            PaxosValue value = readLog(curSeq);
            outputValue = handler.process(curSeq, value);
            curSeq++;
        }
        return outputValue;
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
