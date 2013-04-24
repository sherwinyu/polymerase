package com.joshma.polymerase.paxos;

/**
 * Handler to accept PaxosValues and processes them in some manner.
 */
public interface PlayHandler {
    public Object process(int sequenceNumber, PaxosValue loggedValue);
    public PaxosValue getNoop();
}
