package com.joshma.polymerase.rep;

import com.google.common.collect.Maps;

import java.util.Map;

public class LocalReplicationStore implements ReplicationStore {

    private final Map<String, Object> replicatedObjects;

    public LocalReplicationStore() {
        replicatedObjects = Maps.newConcurrentMap();
    }

    @Override
    public void register(String id, Object original) {
        System.err.printf("Mapped %s => %s\n", id, original);
        replicatedObjects.put(id, original);
    }

    public Object get(String id) {
        return replicatedObjects.get(id);
    }

}
