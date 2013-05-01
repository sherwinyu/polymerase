package com.joshma.polymerase.rep;

import com.google.common.collect.Maps;
import com.joshma.polymerase.net.ServerIdentifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Waits until a ready message has been received from all other servers.
 */
public class Barrier {

    private final static String BARRIER_ACK = "BARRIER_ACK";
    private final List<ServerIdentifier> servers;
    private final ServerIdentifier myIdentifier;
    private final Map<ServerIdentifier, Boolean> readyServers;
    private volatile int readyCount;

    public Barrier(List<ServerIdentifier> servers, ServerIdentifier myIdentifier) {
        this.servers = servers;
        this.myIdentifier = myIdentifier;
        this.readyCount = 0;
        this.readyServers = Maps.newConcurrentMap();
    }

    synchronized private void markReady(ServerIdentifier identifier) {
        if (!readyServers.containsKey(identifier)) {
            readyServers.put(identifier, true);
            readyCount++;
        }
    }

    public void await() {
        // Set to 1 for ourselves.
        readyCount = 1;

        // Send out-going confirmations.
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ServerIdentifier identifier : servers) {
                    if (identifier.equals(myIdentifier)) {
                        continue;
                    }
                    try {
                        Socket outSocket = identifier.createSocket();
                        BufferedReader in = new BufferedReader(new InputStreamReader(outSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(outSocket.getOutputStream(), true);

                        out.println(myIdentifier.toString());
                        // Wait for response.
                        String response = in.readLine();
                        if (BARRIER_ACK.equals(response)) {
                            markReady(identifier);
                        }

                        in.close();
                        out.close();
                        outSocket.close();
                    } catch (IOException e) {
                        // Problem connecting.
                    }
                }
            }
        }).start();

        // Accept in-coming confirmations.
        ServerSocket barrierSocket = null;
        try {
            barrierSocket = new ServerSocket(myIdentifier.getPort());
            barrierSocket.setSoTimeout(1000);
            while (readyCount < servers.size()) {
                Socket client;
                try {
                    client = barrierSocket.accept();
                } catch (IOException e) {
                    // Just a timeout, carry on.
                    continue;
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                ServerIdentifier identifier = ServerIdentifier.parse(in.readLine().trim());
                markReady(identifier);
                out.println(BARRIER_ACK);

                in.close();
                out.close();
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error waiting for all servers to initialize.", e);
        } finally {
            if (barrierSocket != null) {
                try {
                    barrierSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
