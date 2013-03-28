package com.joshma.polymerase.net;

import com.joshma.polymerase.InvalidServerException;

/**
 * Represent a server via hostname:port combination. hostname is null for loopback address.
 * @author joshma
 */
public class ServerIdentifier {
    private final String hostname;
    private final int port;

    public ServerIdentifier(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String toString() {
        return hostname + ":" + port;
    }

    /**
     * Parses a descriptor in the format of either [port] or [hostname]:[port].
     * @param hostdescriptor Descriptor to parse.
     * @return Parsed ServerImpl object.
     */
    static public ServerIdentifier parse(String hostdescriptor) throws InvalidServerException {
        String hostname;
        String portString;

        int colonPosition = hostdescriptor.indexOf(':');
        if (colonPosition >= 0) {
            // If colon is present, assume hostname:port format.
            hostname = hostdescriptor.substring(0, colonPosition);
            if (colonPosition + 1 >= hostdescriptor.length()) {
                throw new InvalidServerException("Invalid server string: " + hostdescriptor);
            }
            portString = hostdescriptor.substring(colonPosition + 1);
        } else {
            // Otherwise, assume just port.
            // Loopback interface.
            hostname = null;
            portString = hostdescriptor;
        }
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            throw new InvalidServerException("Invalid server string: " + hostdescriptor, e);
        }

        return new ServerIdentifier(hostname, port);

    }
}
