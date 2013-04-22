package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import com.joshma.polymerase.rep.InvalidServerException;
import com.joshma.polymerase.rep.Replicator;
import com.joshma.polymerase.rep.ReplicatorImpl;
import org.junit.Test;

import java.util.List;

public class ConnectionTest {

    @Test
    public void testConnect() {
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
                    try {
                        Replicator r = new ReplicatorImpl(serverStrings, j);
                        r.initialize();
                    } catch (InvalidServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
