package com.orderddos.network.loader;

import com.orderddos.network.LoadStatistics;
import com.orderddos.network.decisions.ChangeAmountOfConnections;
import com.orderddos.network.netty.HttpGetNettyNetworkLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // create Options object
        Options options = new Options();
        options.addOption("d", true, "duration");
        options.addOption("c", true, "connections");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String lastArg;
        if (args.length == 0) {
            lastArg = "https://order-ddos.com/";
        } else {
            lastArg = args[args.length - 1];
        }

        String connectionsString = cmd.getOptionValue("c");
        if (connectionsString == null) {
            connectionsString = "10";
        }
        int connections = Integer.parseInt(connectionsString);
        String durationString = cmd.getOptionValue("d");
        if (durationString == null) {
            durationString = "10s";
        }

        int secondsDuration = 0;
        if (durationString.endsWith("s")) {
            secondsDuration = Integer.parseInt(durationString.substring(0, durationString.length() - 1));
        } else if (durationString.endsWith("m")) {
            secondsDuration = 60 * Integer.parseInt(durationString.substring(0, durationString.length() - 1));
        }
        HttpGetNettyNetworkLoader httpGetNettyNetworkLoader = new HttpGetNettyNetworkLoader();
        httpGetNettyNetworkLoader.loadAddress(lastArg,
                loadStatistics ->
                {
                    LoadStatistics poll = loadStatistics.pollLast();
                    int difference = Math.toIntExact(connections - poll.getConnectionsCount());
                    return new ChangeAmountOfConnections(difference);
                }
        );
        Thread.sleep(secondsDuration * 1000);
        System.exit(0);
    }
}
