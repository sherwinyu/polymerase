package com.joshma.polymerase.paxos;

import java.io.Serializable;

public class PrepareResponse implements Serializable {

    public final Status status;
    public final int n;
    public final PaxosValue v;

    public PrepareResponse(Status status) {
        this.status = status;
        this.n = 0;
        this.v = null;
    }

    public PrepareResponse(Status status, int n, PaxosValue v) {
        this.status = status;
        this.n = n;
        this.v = v;
    }

    public String toString() {
        return String.format("PrepareResponse[%s, %d, %s]", status, n, v);
    }
}
