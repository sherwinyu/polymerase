package com.joshma.polymerase.paxos;

public class PaxosInstance {

    private int np;
    private int na;
    private PaxosValue va;
    private PaxosValue decidedValue;

    public PaxosInstance() {
        this.np = -1;
        this.na = -1;
        this.va = null;
        this.decidedValue = null;
    }

    public synchronized PaxosValue getDecidedValue() {
        return decidedValue;
    }

    public synchronized PrepareResponse prepare(int n) {
        if (n > np) {
            np = n;
            return new PrepareResponse(Status.OK, na, va);
        }
        return new PrepareResponse(Status.REJECT);
    }

    public synchronized AcceptResponse accept(int n, PaxosValue v) {
        if (n >= np) {
            np = n;
            na = n;
            va = v;
            return new AcceptResponse(Status.OK);
        }
        return new AcceptResponse(Status.REJECT);
    }

    public synchronized void decide(PaxosValue v) {
        decidedValue = v;
    }
}
