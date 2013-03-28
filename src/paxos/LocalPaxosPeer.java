package com.joshma.polymerase.paxos;

import com.google.common.collect.Lists;
import com.joshma.polymerase.Event;

import java.rmi.RemoteException;
import java.util.List;

public class LocalPaxosPeer implements PaxosPeer {

    private final List<PaxosPeer> peers;

    public LocalPaxosPeer() {
        peers = Lists.newArrayList();
    }

    public void initialize(List<PaxosPeer> peers) throws RemoteException {
        // Add all peers, including yourself.
        peers.clear();
        peers.addAll(peers);
    }

    public void start(int sequenceNumber, Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean status(int sequenceNumber) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PrepareResponse prepare(int sequenceNumber, int n) throws RemoteException {
        System.err.printf("PREPARING FOR %d, CALLED BY %d\n", sequenceNumber, n);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AcceptResponse accept(int sequenceNumber, int n, Event event) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void decide(int sequenceNumber, Event event) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
