package com.joshma.polymerase.rep;

/**
 * General interface for a replicated service.
 * @author joshma
 */
public interface Replicator {

    public void initialize() throws InvalidServerException;

}
