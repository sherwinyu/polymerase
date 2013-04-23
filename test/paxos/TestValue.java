package com.joshma.polymerase.paxos;

import java.util.UUID;

public class TestValue implements PaxosValue {

    private final int n;
    private final UUID uuid;

    public TestValue(int n) {
        this.n = n;
        this.uuid = UUID.randomUUID();
    }

    public String toString() {
        return String.format("VALUE[%d]", n);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
