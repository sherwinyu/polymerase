package com.joshma.polymerase.paxos;

public class TestValue implements PaxosValue {

    private final int n;

    public TestValue(int n) {
        this.n = n;
    }

    public int getN() {
        return n;
    }

    public String toString() {
        return String.format("VALUE[%d]", n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestValue testValue = (TestValue) o;

        if (n != testValue.n) return false;

        return true;
    }
}
