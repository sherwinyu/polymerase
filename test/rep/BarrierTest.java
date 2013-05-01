package com.joshma.polymerase.rep;

import com.google.common.collect.Lists;
import com.joshma.polymerase.net.ServerIdentifier;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BarrierTest {

    @Test
    public void testBarrierBasic() throws InterruptedException {
        final List<ServerIdentifier> servers = Lists.newArrayList();
        final int numServers = 8;
        for (int i = 5000; i < 5000 + numServers; i++) {
            servers.add(new ServerIdentifier(null, i));
        }
        final CountDownLatch doneSignal = new CountDownLatch(numServers);
        for (int i = 0; i < numServers - 1; i++) {
            final int me = i;
            new Thread(new Runnable() {
                public void run() {
                    System.err.printf("Starting thread %d\n", me);
                    new Barrier(servers, servers.get(me)).await();
                    System.err.printf("Finished thread %d\n", me);
                    doneSignal.countDown();
                }
            }).start();
        }
        // Start the last one late.
        Thread.sleep(2000);
        System.err.println("Starting the last one!");
        new Barrier(servers, servers.get(numServers - 1)).await();
        System.err.println("Done");
    }

}
