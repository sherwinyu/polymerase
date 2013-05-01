package com.joshma.polymerase;

import com.google.common.collect.Lists;
import com.joshma.polymerase.net.ServerIdentifier;
import com.joshma.polymerase.rep.Replicated;
import com.joshma.polymerase.rep.Replicator;
import com.joshma.polymerase.rep.ReplicatorImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Server to illustrate API.
 */
public class StandaloneServer {

    private static final String PROPFILE_NAME = "standalone.prop";
    private static final String SERVER_PROP_NAME = "SERVERS";
    private static final String CREATE_CMD = "create";
    private static final String APPEND_CMD = "append";
    private static final String LOOKUP_CMD = "lookup";

    private final List<String> serverStrings;
    private final int me;

    public StandaloneServer(List<String> serverStrings, int me) {
        this.serverStrings = serverStrings;
        this.me = me;
    }

    public void start() throws IOException {
        Replicator replicator = new ReplicatorImpl(serverStrings, me);
        replicator.initialize();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(">> ");
            String input = in.readLine().trim();
            if (!input.isEmpty()) {
                String[] tokens = input.split(" ");
                String command = tokens[0];
                if (CREATE_CMD.equals(command)) {
                    System.out.println("Creating replicated ArrayList");
                    Replicated<ArrayList<String>> replicated = replicator.replicate(new ArrayList<String>());
                    System.out.println("Replicated id:");
                    System.out.println(replicated.getId());
                    continue;
                }
                if (APPEND_CMD.equals(command) && tokens.length == 3) {
                    String id = tokens[1];
                    String value = tokens[2];
                    System.out.println("Appending " + value + " to " + id);
                    List<String> array = replicator.get(id);
                    array.add(value);
                    continue;
                }
                if (LOOKUP_CMD.equals(command) && tokens.length == 2) {
                    String id = tokens[1];
                    List<String> array = replicator.get(id);
                    System.out.println("Array: " + array);
                    continue;
                }
            }
            System.out.println("Invalid command");
        }
    }

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
            StandaloneServer server = new StandaloneServer(serverStrings, me);
            server.start();
        } catch (NumberFormatException e) {
            System.out.println("Invalid `me` param, must be integer: " + args[0]);
            System.exit(-1);
        }
    }

}
