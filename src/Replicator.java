package com.joshma.polymerase;

import java.io.IOException;

/**
 * General interface for a replicated service.
 * @author joshma
 */
public interface Replicator {

    public void initialize() throws InvalidServerException;

}
