package com.joshma.polymerase.rep;

/**
 * Indicates server string was not in the format port or hostname:port.
 */
public class InvalidServerException extends RuntimeException {

    public InvalidServerException(String message) {
        super(message);
    }

    public InvalidServerException(String message, Exception e) {
        super(message, e);
    }
}
