package com.orderddos.agent;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.orderddos.network.netty.HttpGetNettyNetworkLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OrderDDoSAgent {

    private static final Logger log = LoggerFactory.getLogger(OrderDDoSAgent.class);

    private static final String CARBON_HOST = "storage.order-ddos.com";
    private static final Integer CARBON_PORT = 2003;

    public static void main(String[] args) throws Exception {
        UUID uuid;
        String uri;
        if (args.length == 2) {
            uuid = UUID.fromString(args[0]);
            uri = args[1];
        } else {
            uuid = UUID.fromString("b7f9fef8-3e7b-41fd-9f2a-0f5fe5ffe4b1");
            uri = "https://order-ddos.com/";
        }
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
        Histogram histogram = metricRegistry.histogram(String.format("request.time.%s", uuid.toString()));
        HttpGetNettyNetworkLoader httpGetNettyNetworkLoader = new HttpGetNettyNetworkLoader();
        NetworkLoaderDecisionEngine decisionEngine = new NetworkLoaderDecisionEngine(
                bytesReadPerSecond,
                bytesWrittenPerSecond,
                traffic
        );
        httpGetNettyNetworkLoader.loadAddress(uri, decisionEngine,
                event -> succeedRequestsPerSecond.mark(),
                event -> failedRequestsPerSecond.mark(),
                histogram::update
        );
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
    }
}
