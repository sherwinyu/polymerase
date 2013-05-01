package com.joshma.polymerase.rep;

public class Replicated<T> {

    private final T obj;
    private final String id;

    public Replicated(T obj, String id) {
        this.obj = obj;
        this.id = id;
    }

    public T getObj() {
        return obj;
    }

    public String getId() {
        return id;
    }

}
