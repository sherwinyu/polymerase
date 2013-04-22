package com.joshma.polymerase.paxos;

import com.joshma.polymerase.Event;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Runs Paxos in a thread.
 */
public class PaxosThread extends Thread {

    private final PaxosInstance instance;
    private final List<PaxosPeer> peers;
    private final int me;

    public PaxosThread(PaxosInstance instance, List<PaxosPeer> peers, int me) {
        this.instance = instance;
        this.peers = peers;
        this.me = me;
    }

    // TODO: Update Done status.
    @Override
    public void run() {
        final int seq = instance.getSequence();
        final int majority = (peers.size() / 2) + 1;
        
        Event proposalV = instance.getProposedEvent();

        try {
            for (int n = me; ; n += peers.size()) {
                int prepareCount = 0;
                int maxN = 0;
                // Send prepare(n)
                for (PaxosPeer peer : peers) {
                    try {
                        PrepareResponse prepareResponse = peer.prepare(seq, n);
                        switch (prepareResponse.status) {
                            case DECIDED:
                                // TODO: handle DECIDED optimization.
                            case OK:
                                prepareCount++;
                                if (prepareResponse.n > maxN) {
                                    proposalV = prepareResponse.v;
                                    maxN = prepareResponse.n;
                                }
                        }
                    } catch (RemoteException e) {
                        System.err.println("RMI exception: " + e.getMessage());
                    }
                }

                if (prepareCount < majority) {
                    Thread.sleep(me * 50);
                    continue;
                }

                // Send accept(n, highestV)
                int acceptCount = 0;
                for (PaxosPeer peer : peers) {
                    try {
                        AcceptResponse acceptResponse = peer.accept(seq, n, proposalV);
                        if (acceptResponse.status == Status.OK) {
                            acceptCount++;
                        }
                    } catch (RemoteException e) {
                        System.err.println("RMI exception: " + e.getMessage());
                    }
                }

                if (acceptCount < majority) {
                    Thread.sleep(me * 50);
                    continue;
                }

                // Complete! Go to decided phase.
                break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Decided
        for (PaxosPeer peer : peers) {
            try {
                peer.decide(seq, proposalV);
            } catch (RemoteException e) {
                System.err.println("RMI exception: " + e.getMessage());
            }
        }
    }

}
