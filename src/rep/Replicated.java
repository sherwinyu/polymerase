package com.joshma.polymerase.rep;

public class Replicated<T> {

    protected final T obj;
    protected final String id;

    public Replicated(T obj, String id) {
        this.obj = obj;
        this.id = id;
    }

}
