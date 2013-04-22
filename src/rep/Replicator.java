package com.joshma.polymerase.rep;

/**
 * General interface for a replicated service.
 */
public interface Replicator {

    public void initialize() throws InvalidServerException;

}
