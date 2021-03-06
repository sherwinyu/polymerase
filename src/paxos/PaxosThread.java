package com.joshma.polymerase.paxos;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Runs Paxos in a thread.
 */
public class PaxosThread extends Thread {

    private final List<PaxosPeer> peers;
    private final int me;
    private final int sequenceNumber;
    private final PaxosValue proposedValue;

    public PaxosThread(int sequenceNumber, PaxosValue proposedValue, List<PaxosPeer> peers, int me) {
        this.sequenceNumber = sequenceNumber;
        this.proposedValue = proposedValue;
        this.peers = peers;
        this.me = me;
    }

    // TODO: Update Done status.
    @Override
    public void run() {
        final int seq = sequenceNumber;
        final int majority = (peers.size() / 2) + 1;

        PaxosValue proposalV = proposedValue;
        System.err.printf("Starting paxos for seq=%d, value=%s\n", seq, proposalV);

        try {
            for (int n = me; ; n += peers.size()) {
                int prepareCount = 0;
                int maxN = -1;
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
                System.err.printf("[%d] Accepting seq=%d, value=%s\n", me, seq, proposalV);
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
        System.err.printf("[%d] Done: %s for seq=%d\n", me, proposalV, seq);
        for (PaxosPeer peer : peers) {
            try {
                peer.decide(seq, proposalV);
            } catch (RemoteException e) {
                System.err.println("RMI exception: " + e.getMessage());
            }
        }
    }

}
