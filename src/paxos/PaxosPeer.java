package com.joshma.polymerase.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A Paxos peer. Proposes and agrees on values.
 */
public interface PaxosPeer extends Remote {

    public void start(int sequenceNumber, PaxosValue value) throws RemoteException;
    public PrepareResponse prepare(int sequenceNumber, int n) throws RemoteException;
    public AcceptResponse accept(int sequenceNumber, int n, PaxosValue value) throws RemoteException;
    public void decide(int sequenceNumber, PaxosValue value) throws RemoteException;
    public PaxosValue status(int sequenceNumber);

}
