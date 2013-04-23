package com.joshma.polymerase.paxos;

/**
 * Handler to accept PaxosValues and processes them in some manner.
 */
public interface PlayHandler {
    public void process(int sequenceNumber, PaxosValue loggedValue);
}
