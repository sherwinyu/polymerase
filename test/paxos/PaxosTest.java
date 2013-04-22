package com.joshma.polymerase.paxos;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;

public class PaxosTest {

    private static final int NUM_PEERS = 10;
    private List<PaxosPeer> peers;

    private int nDecided(List<PaxosPeer> peers, int seq) {
        int count = 0;
        PaxosValue decidedValue = null;
        for (PaxosPeer peer : peers) {
            PaxosValue value = peer.status(seq);
            if (value != null) {
                if (count > 0 && !value.equals(decidedValue)) {
                    fail(String.format("Decided values do not match: seq=%d, peer=%s, v=%s, v1=%s",
                            seq, peer, decidedValue, value));
                }
                decidedValue = value;
                count++;
            }
        }
        return count;
    }

    private void waitN(List<PaxosPeer> peers, int seq, int wantedN) throws InterruptedException {
        int timeout = 10;
        for (int i = 0; i < 30; i++) {
            if (nDecided(peers, seq) >= wantedN) {
                break;
            }
            Thread.sleep(timeout);
            if (timeout < 1000) {
                timeout *= 2;
            }
        }
        int n = nDecided(peers, seq);
        if (n < wantedN) {
            fail(String.format("Too few decided, seq=%d n=%d wanted=%d", seq, n, wantedN));
        }
    }

    private void waitAll(List<PaxosPeer> peers, int seq) throws InterruptedException {
        waitN(peers, seq, peers.size());
    }

    @Before
    public void setup() throws RemoteException {
        peers = Lists.newArrayList();
        for (int i = 0; i < NUM_PEERS; i++) {
            peers.add(new LocalPaxosPeer(i));
        }
        for (PaxosPeer peer : peers) {
            ((LocalPaxosPeer) peer).initialize(peers);
        }
    }

    @Test
    public void testSingleInstance() throws RemoteException, InterruptedException {
        int seqNum = 2;
        TestValue testValue = new TestValue(5);
        peers.get(0).start(seqNum, testValue);

        waitAll(peers, seqNum);
    }

    @Test
    public void testMultiplePropSameValue() throws RemoteException, InterruptedException {
        int seqNum = 3;
        TestValue testValue = new TestValue(10);
        peers.get(2).start(seqNum, testValue);
        peers.get(3).start(seqNum, testValue);
        peers.get(4).start(seqNum, testValue);

        waitAll(peers, seqNum);
    }

    @Test
    public void testMultiplePropDifferentValue() throws RemoteException, InterruptedException {
        int seqNum = 4;
        peers.get(2).start(seqNum, new TestValue(10));
        peers.get(3).start(seqNum, new TestValue(11));
        peers.get(4).start(seqNum, new TestValue(12));

        waitN(peers, seqNum, peers.size());
    }

    @Test
    public void testOutOfOrder() throws RemoteException, InterruptedException {
        int seqNum = 5;
        peers.get(0).start(7, new TestValue(700));
        peers.get(0).start(6, new TestValue(600));
        peers.get(1).start(5, new TestValue(500));

        waitAll(peers, 7);

        peers.get(0).start(4, new TestValue(400));
        peers.get(1).start(3, new TestValue(300));

        waitAll(peers, 6);
        waitAll(peers, 5);
        waitAll(peers, 4);
        waitAll(peers, 3);
    }

    @Test
    public void testMany() throws RemoteException, InterruptedException {
        final int maxSeq = 100;
        for (int seq = 1; seq < maxSeq; seq++) {
            for (int i = 0; i < peers.size(); i++) {
                PaxosPeer peer = peers.get(i);
                peer.start(seq, new TestValue((seq * 10) + i));
            }
        }

        boolean done = false;
        while (!done) {
            done = true;
            for (int seq = 1; seq < maxSeq; seq++) {
                if (nDecided(peers, seq) < peers.size()) {
                    done = false;
                    break;
                }
            }
            Thread.sleep(100);
        }
    }
}
