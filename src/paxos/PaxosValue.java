package com.joshma.polymerase.paxos;

import java.io.Serializable;
import java.util.UUID;

/**
 * Objects that Paxos agrees on must implement this interface.
 */
public interface PaxosValue extends Serializable {

    public UUID getUUID();

}
