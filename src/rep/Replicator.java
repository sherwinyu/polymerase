package com.joshma.polymerase.rep;

/**
 * General interface for a replicated service.
 */
public interface Replicator {

    public void initialize() throws InvalidServerException;
    public <T> Replicated<T> replicate(T delegate);
    public Object get(String id);

}
