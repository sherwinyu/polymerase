package com.joshma.polymerase.paxos;

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

    @Override
    public void run() {
        final int majority = (peers.size() / 2) + 1;
        for (int n = me;; n += peers.size()) {
            int prepareCount = 0;
            int acceptCount = 0;
            
        }
    }
}
