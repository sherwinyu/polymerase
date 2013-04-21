package com.joshma.polymerase.paxos;

import com.joshma.polymerase.Event;

public class PaxosInstance {

    private final int seq;
    private final Event event;

    public PaxosInstance(int seq, Event event) {
        this.seq = seq;
        this.event = event;
    }

    public Event getValue() {
        // TODO: Return actual agreed-on event.
        return null;
    }

    public PrepareResponse prepare(int n) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public AcceptResponse accept(int n) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public void decide(Event event) {

    }

    public int getSequence() {
        return seq;
    }
}
