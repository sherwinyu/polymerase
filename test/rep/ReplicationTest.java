package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class ReplicationTest {

    volatile String replicatedId;

    @Test
    public void testBasicReplication() throws InterruptedException {
        final int basePort = 5000;
        final int numInstances = 5;
        final List<String> serverStrings = Lists.newArrayList();
        for (int i = 0; i < numInstances; i++) {
            serverStrings.add(String.valueOf(basePort + i));
        }

        System.err.println("Strings: " + serverStrings);

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(numInstances);

        replicatedId = null;

        for (int i = 0; i < numInstances; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Replicator r = new ReplicatorImpl(serverStrings, j);

                        startSignal.await();

                        r.initializeStore();
                        if (j == 0) {
                            // Initialize the test one on this thread.
                            Replicated<HashMap<String, String>> repValue = r.replicate(new HashMap<String, String>());
                            Map<String, String> replicatedMap = repValue.obj;
                            replicatedId = repValue.id;

                            replicatedMap.put("k1", "v1");
                            assertEquals("v1", replicatedMap.get("k1"));
                        }
                        if (j == 2) {
                            while (replicatedId == null) {
                                Thread.sleep(100);
                            }
                            Map<String, String> remoteMap = (Map<String, String>) r.get(replicatedId);
                            assertEquals("v1", remoteMap.get("k1"));
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    doneSignal.countDown();
                }
            }).start();
        }

        startSignal.countDown();
        doneSignal.await();

    }

}
