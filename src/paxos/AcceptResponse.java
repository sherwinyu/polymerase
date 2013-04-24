package com.joshma.polymerase.paxos;

import java.io.Serializable;

public class AcceptResponse implements Serializable {

    public final Status status;

    public AcceptResponse(Status status) {
        this.status = status;
    }
}
