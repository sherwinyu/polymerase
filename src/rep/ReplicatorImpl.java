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
import java.util.UUID;

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
    private LocalReplicationStore localStore;

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
        System.err.printf("Initializng server me=%d...\n", me);
        final List<ServerIdentifier> parsedServers = Lists.newArrayList();
        // Initialize barrierServers to be parsedServers with port + 1024
        final List<ServerIdentifier> barrierServers = Lists.newArrayList();

        // Validate serverStrings first.
        for (String serverstring : serverStrings) {
            ServerIdentifier server = ServerIdentifier.parse(serverstring);
            parsedServers.add(server);
            barrierServers.add(new ServerIdentifier(server.getHostname(), server.getPort() + 1024));
        }

        try {
            // Bind remote factories and peers over RMI.
            Registry registry = LocateRegistry.createRegistry(parsedServers.get(me).getPort());

            localStore = new LocalReplicationStore();
            ReplicationStore factoryStub = (ReplicationStore) UnicastRemoteObject.exportObject(localStore, 0);
            registry.bind(RMI_FACTORY_NAME, factoryStub);

            List<PaxosPeer> peers = Lists.newArrayList();
            PaxosPeer stub = (PaxosPeer) UnicastRemoteObject.exportObject(peer, 0);
            registry.bind(RMI_PAXOS_PEER_NAME, stub);

            System.err.println("Waiting for all servers to be ready...");
            new Barrier(barrierServers, barrierServers.get(me)).await();
            System.err.println("All servers ready, setting up peers and factories.");

            for (int i = 0; i < parsedServers.size(); i++) {
                // Handle local case.
                if (i == me) {
                    peers.add(peer);
                    stores.add(localStore);
                    continue;
                }

                // Otherwise add the RMI'd version.
                ServerIdentifier serverIdentifier = parsedServers.get(i);
                Registry remoteRegistry = LocateRegistry.getRegistry(serverIdentifier.getHostname(),
                        serverIdentifier.getPort());
                PaxosPeer remotePeer = (PaxosPeer) remoteRegistry.lookup(RMI_PAXOS_PEER_NAME);
                peers.add(remotePeer);

                ReplicationStore remoteFactory = (ReplicationStore) remoteRegistry.lookup(RMI_FACTORY_NAME);
                stores.add(remoteFactory);
            }

            ((LocalPaxosPeer) peer).initialize(peers);

            System.err.println("Initialization complete");

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
    public <T> Replicated<T> replicate(T delegate) {
        // Get a unique ID.
        String id = UUID.randomUUID().toString();

        try {
            // Create the object on remote servers.
            for (ReplicationStore store : stores) {
                store.register(id, delegate);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }

        InvocationHandler handler = new ReplicationHandler(me, id, peer, localStore);
        T replicatedObj = (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                delegate.getClass().getInterfaces(), handler);

        return new Replicated<T>(replicatedObj, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String id) {
        Object delegate = localStore.get(id);
        InvocationHandler handler = new ReplicationHandler(me, id, peer, localStore);
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                delegate.getClass().getInterfaces(), handler);
    }
}
