package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class ReplicationTest {

    volatile String replicatedId;
    volatile String testValue0;
    volatile String testValue1;
    volatile String testValue2;

    final private int BASEPORT = 5000;
    private List<String> serverStrings;
    final private int NUM_INSTANCES = 5;

    @Before
    public void setup() {
        serverStrings = Lists.newArrayList();
        for (int i = 0; i < NUM_INSTANCES; i++) {
            serverStrings.add(String.valueOf(BASEPORT + i));
        }

        System.err.println("Strings: " + serverStrings);
    }

    @Test
    public void testBasicReplication() throws InterruptedException {
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(NUM_INSTANCES);

        replicatedId = null;
        testValue0 = null;
        testValue1 = null;
        testValue2 = null;

        for (int i = 0; i < NUM_INSTANCES; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Replicator r = new ReplicatorImpl(serverStrings, j);

                        startSignal.await();

                        r.initialize();
                        if (j == 0) {
                            // Initialize the test one on this thread.
                            Replicated<HashMap<String, String>> repValue = r.replicate(new HashMap<String, String>());
                            Map<String, String> replicatedMap = repValue.getObj();
                            replicatedId = repValue.getId();

                            replicatedMap.put("k1", "v1");
                            testValue0 = replicatedMap.get("k1");
                        }

                        if (j == 2) {
                            while (replicatedId == null) {
                                Thread.sleep(100);
                            }
                            Map<String, String> remoteMap = r.get(replicatedId);
                            testValue1 = remoteMap.get("k1");
                        }
                        if (j == 3) {
                            while (replicatedId == null) {
                                Thread.sleep(100);
                            }
                            Map<String, String> remoteMap = r.get(replicatedId);
                            testValue2 = remoteMap.get("k1");
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (AssertionError e) {
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }
            }).start();
        }

        startSignal.countDown();
        doneSignal.await();

        assertEquals("v1", testValue0);
        assertEquals("v1", testValue1);
        assertEquals("v1", testValue2);

    }

}
