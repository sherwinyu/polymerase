package com.joshma.polymerase.paxos;

import java.util.UUID;

/**
 * Objects that Paxos agrees on must implement this interface.
 */
public interface PaxosValue {

    public UUID getUUID();

}
