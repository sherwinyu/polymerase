package com.joshma.polymerase.rep;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicationStore extends Remote {

    /**
     * Accepts remote object and tracks it locally for replication.
     *
     * @param id       The ID to associate with the object - this is assigned to be unique per object.
     * @param original Object to replicate
     */
    public void register(String id, Object original) throws RemoteException;

}
