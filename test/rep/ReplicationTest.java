package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ReplicationTest {

    @Test
    public void testBasicReplication() throws InterruptedException {
        final int basePort = 5000;
        final int numInstances = 5;
        final List<String> serverStrings = Lists.newArrayList();
        for (int i = 0; i < numInstances; i++) {
            serverStrings.add(String.valueOf(basePort + i));
        }

        System.err.println("Strings: " + serverStrings);

        for (int i = 0; i < numInstances; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Replicator r = new ReplicatorImpl(serverStrings, j);
                    r.initialize();

                    if (j == 0) {
                        Map<String, String> storeMap = Maps.newHashMap();
                        Map<String, String> replicatedMap = r.replicate(storeMap);
                        replicatedMap.put("k1", "v1");
                    }
                }
            }).start();
        }

        Thread.sleep(2000);
    }

}
