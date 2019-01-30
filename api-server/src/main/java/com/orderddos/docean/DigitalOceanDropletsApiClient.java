package com.orderddos.docean;

import com.orderddos.domain.App;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalOceanDropletsApiClient {

    private static final Logger log = LoggerFactory.getLogger(DigitalOceanDropletsApiClient.class.getName());

    private WebClient webClient;

    public DigitalOceanDropletsApiClient(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
    }

    public DigitalOceanDropletsApiClient() {
        this(Vertx.vertx());
    }

    public Future<Void> createNewDroplet(CreateNewDroplet newDroplet) {
        HttpRequest<Buffer> request = webClient.postAbs("https://api.digitalocean.com/v2/droplets");
        request.headers().add("Authorization", "Bearer " + App.OTABEK_API_KEY);
        request.sendJsonObject(JsonObject.mapFrom(newDroplet), event -> {
            if (event.succeeded()) {
                log.info("Done");
            } else {
                log.error("Unable to create a droplet " + newDroplet, event.cause());
            }
        });
        return null;
    }
}
