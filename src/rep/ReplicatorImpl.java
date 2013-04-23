package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import com.joshma.polymerase.net.ServerIdentifier;
import com.joshma.polymerase.paxos.LocalPaxosPeer;
import com.joshma.polymerase.paxos.PaxosPeer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * The core class in this package. Instantiates and maintains server connections and creates
 * proxied, replicated objects.
 */
public class ReplicatorImpl implements Replicator {

    private final static String RMI_FACTORY_NAME = "RmiFactory";
    private final static String RMI_PAXOS_PEER_NAME = "RmiPaxosPeer";
    private final List<String> serverStrings;
    private final int me;
    private final PaxosPeer peer;
    private final List<ReplicationStore> stores;

    /**
     * Creates a set of replicated serverStrings.
     *
     * @param serverStrings List of hostname:port strings.
     * @param me            The index corresponding to this server.
     */
    public ReplicatorImpl(List<String> serverStrings, int me) {
        this.serverStrings = serverStrings;
        this.me = me;
        this.peer = new LocalPaxosPeer(me);
        this.stores = Lists.newArrayList();
    }

    @Override
    public void initialize() throws InvalidServerException {
        System.err.println("Initializing server me=" + me);
        final List<ServerIdentifier> parsedServers = Lists.newArrayList();

        // Validate serverStrings first.
        for (String serverstring : serverStrings) {
            ServerIdentifier server = ServerIdentifier.parse(serverstring);
            parsedServers.add(server);
        }

        try {
            // Bind remote factories and peers over RMI.
            Registry registry = LocateRegistry.createRegistry(parsedServers.get(me).getPort());

            ReplicationStore store = new ReplicationStoreImpl();
            ReplicationStore factoryStub = (ReplicationStore) UnicastRemoteObject.exportObject(store, 0);
            registry.bind(RMI_FACTORY_NAME, factoryStub);

            PaxosPeer peer = new LocalPaxosPeer(me);
            List<PaxosPeer> peers = Lists.newArrayList();
            PaxosPeer stub = (PaxosPeer) UnicastRemoteObject.exportObject(peer, 0);
            registry.bind(RMI_PAXOS_PEER_NAME, stub);

            for (int i = 0; i < parsedServers.size(); i++) {
                // Handle local case.
                if (i == me) {
                    peers.add(peer);
                    stores.add(store);
                    continue;
                }

                // Otherwise add the RMI version.
                ServerIdentifier serverIdentifier = parsedServers.get(i);
                Registry remoteRegistry = LocateRegistry.getRegistry(serverIdentifier.getHostname(),
                        serverIdentifier.getPort());
                PaxosPeer remotePeer = (PaxosPeer) remoteRegistry.lookup(RMI_PAXOS_PEER_NAME);
                peers.add(remotePeer);

                ReplicationStore remoteFactory = (ReplicationStore) remoteRegistry.lookup(RMI_FACTORY_NAME);
                stores.add(remoteFactory);
            }

            ((LocalPaxosPeer) peer).initialize(peers);
            System.err.printf("[%d] Done initializing\n", me);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T replicate(T delegate) {
        // Get a unique ID.
        String id = String.format("%s-%d", delegate.getClass().getCanonicalName(), delegate.hashCode());

        // TODO better ID handling - should log to Paxos to get guaranteed unique ID.
        try {
            // Create the object on remote servers.
            for (ReplicationStore store : stores) {
                store.register(id, delegate);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }

        InvocationHandler handler = new ReplicationHandler(id, peer);
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                delegate.getClass().getInterfaces(), handler);
    }
}
