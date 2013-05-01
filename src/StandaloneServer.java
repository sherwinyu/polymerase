package com.joshma.polymerase;

import com.google.common.collect.Lists;
import com.joshma.polymerase.net.ServerIdentifier;
import com.joshma.polymerase.rep.Replicator;
import com.joshma.polymerase.rep.ReplicatorImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Server to illustrate API.
 */
public class StandaloneServer {

    private static final String PROPFILE_NAME = "standalone.prop";
    private static final String SERVER_PROP_NAME = "SERVERS";

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server!");
        // Initialize properties.
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(PROPFILE_NAME);
        props.load(in);
        in.close();

        // Parse out configuration variables. SERVERS is comma-separated.
        List<String> serverStrings = Lists.newArrayList(props.getProperty(SERVER_PROP_NAME).split(","));
        if (args.length < 1) {
            System.out.println("Must specify `me` as first parameter.");
            System.exit(-1);
        }
        try {
            int me = Integer.parseInt(args[0]);
            Replicator replicator = new ReplicatorImpl(serverStrings, me);
            replicator.initialize();
        } catch (NumberFormatException e) {
            System.out.println("Invalid `me` param, must be integer: " + args[0]);
            System.exit(-1);
        }
    }

}
