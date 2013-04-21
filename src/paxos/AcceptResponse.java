package com.joshma.polymerase.paxos;

public class AcceptResponse {

    public final Status status;

    public AcceptResponse(Status status) {
        this.status = status;
    }
}
