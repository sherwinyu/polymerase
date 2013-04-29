package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class ConnectionTest {

    @Test
    public void testConnect() throws InterruptedException {
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
                    r.initializeStore();
                }
            }).start();
        }

        Thread.sleep(2000);
    }

}
