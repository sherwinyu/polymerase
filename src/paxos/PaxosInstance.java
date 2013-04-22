package com.joshma.polymerase.paxos;

import com.joshma.polymerase.Event;

public class PaxosInstance {

    private final int seq;
    private final Event proposedEvent;
    private int np;
    private int na;
    private Event va;
    private Event decidedEvent;

    public PaxosInstance(int seq, Event event) {
        this.seq = seq;
        this.proposedEvent = event;
        this.np = -1;
        this.na = -1;
        this.va = null;
        this.decidedEvent = null;
    }

    public Event getDecidedEvent() {
        return decidedEvent;
    }

    public Event getProposedEvent() {
        return proposedEvent;
    }

    public PrepareResponse prepare(int n) {
        if (n > np) {
            np = n;
            return new PrepareResponse(Status.OK, na, va);
        }
        return new PrepareResponse(Status.REJECT);
    }

    public AcceptResponse accept(int n, Event v) {
        if (n >= np) {
            np = n;
            na = n;
            va = v;
            return new AcceptResponse(Status.OK);
        }
        return new AcceptResponse(Status.REJECT);
    }

    public void decide(Event event) {
        decidedEvent; = event;
    }

    public int getSequence() {
        return seq;
    }
}
