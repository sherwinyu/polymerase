package com.joshma.polymerase.paxos;

import com.joshma.polymerase.Event;

public class PrepareResponse {

    public final Status status;
    public final int n;
    public final Event v;

    public PrepareResponse(Status status, int n, Event v) {
        this.status = status;
        this.n = n;
        this.v = v;
    }
}
