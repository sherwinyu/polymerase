package com.joshma.polymerase;

import com.google.common.collect.Lists;
import com.joshma.polymerase.net.ServerIdentifier;
import com.joshma.polymerase.paxos.LocalPaxosPeer;
import com.joshma.polymerase.paxos.PaxosPeer;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * The core class in this package. Instantiates and maintains server connections and creates
 * proxied, replicated objects.
 *
 * @author joshma
 */
public class ReplicatorImpl implements Replicator {

    private final static String PAXOS_PEER_NAME = "PaxosPeer";
    private final List<String> serverStrings;
    private final int me;
    private static final int BASE_PORT = 4000;

    /**
     * Creates a set of replicated serverStrings.
     *
     * @param serverStrings List of hostname:port strings.
     * @param me            The index corresponding to this server.
     */
    public ReplicatorImpl(List<String> serverStrings, int me) {
        this.serverStrings = serverStrings;
        this.me = me;
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
            PaxosPeer peer = new LocalPaxosPeer();
            PaxosPeer stub = (PaxosPeer) UnicastRemoteObject.exportObject(peer, 0);
            List<PaxosPeer> peers = Lists.newArrayList();

            // Export for RMI.
            Registry registry = LocateRegistry.createRegistry(parsedServers.get(me).getPort());
            registry.bind(PAXOS_PEER_NAME, stub);
            for (int i = 0; i < parsedServers.size(); i++) {
                if (i == me) {
                    peers.add(peer);
                    continue;
                }
                ServerIdentifier serverIdentifier = parsedServers.get(i);
                Registry remoteRegistry = LocateRegistry.getRegistry(serverIdentifier.getHostname(),
                        serverIdentifier.getPort());
                PaxosPeer remotePeer = (PaxosPeer) remoteRegistry.lookup(PAXOS_PEER_NAME);
                peers.add(remotePeer);
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

//    @SuppressWarnings("unchecked")
//    static public <T> T replicate(T delegate) {
//        InvocationHandler handler = new ReplicationHandler(delegate);
//        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
//                delegate.getClass().getInterfaces(), handler);
//    }
}
