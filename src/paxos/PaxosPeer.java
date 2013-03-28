package com.joshma.polymerase.paxos;

import com.joshma.polymerase.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A Paxos peer. Proposes and agrees on values.
 * @author joshma
 */
public interface PaxosPeer extends Remote {

    public PrepareResponse prepare(int sequenceNumber, int n) throws RemoteException;
    public AcceptResponse accept(int sequenceNumber, int n, Event event) throws RemoteException;
    public void decide(int sequenceNumber, Event event) throws RemoteException;

}
