package com.orderddos.server;

import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import io.reactiverse.pgclient.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ApiService extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    public static final Integer API_SERVICE_VERTICLE_PORT = 4000;

    public static final String START_DDOS = "/start-ddos";
    public static final String DDOS_UUID = "uuid";
    private PgPool pgClient;
    private DigitalOceanClient digitalOceanClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        var httpServer = vertx.createHttpServer();
        this.digitalOceanClient = new DigitalOceanClient(DDoSApiService.OTABEK_API_KEY);
        var router = Router.router(vertx);

        // Pool options
        PgPoolOptions options = new PgPoolOptions()
                .setPort(5432)
                .setHost("104.248.203.116")
                .setDatabase("postgres")
                .setUser("postgres")
                .setPassword("GSJ73vFZ3L6cfZFCBzTtp7JZ5vnCsrpxeuj9squgsu53FkV7NacLmqUZ8PDxyVCY")
                .setMaxSize(5);

        // Create the client pool
        this.pgClient = PgClient.pool(vertx, options);

        router.post(START_DDOS).handler(ctx -> {
            MultiMap params = ctx.request().params();
            UUID uuid = UUID.fromString(params.get(DDOS_UUID));
            pgClient.preparedQuery(
                    "select uuid , t_submitted , email , target_url , num_nodes_by_region , t_start , duration , status from Orders where uuid =$1",
                    Tuple.of(uuid), result -> {
                        if (result.succeeded()) {
                            PgRowSet rows = result.result();
                            Row onlyRow = rows.iterator().next();
                            Order order = new Order(
                                    onlyRow.getUUID("uuid"),
                                    onlyRow.getOffsetDateTime("t_submitted"),
                                    onlyRow.getString("email"),
                                    onlyRow.getString("target_url"),
                                    (JsonObject) onlyRow.getJson("num_nodes_by_region").value(),
                                    onlyRow.getOffsetDateTime("t_start"),
                                    onlyRow.getInterval("duration"),
                                    onlyRow.getString("status")
                            );
                            new DDoSDeployment(digitalOceanClient, vertx, order).deploy().setHandler(deployed -> {
                                if (deployed.succeeded()) {
                                    ctx.response().end("DEPLOYED");
                                } else {
                                    ctx.response().end("ERROR" + deployed.cause().getMessage());
                                }
                            });
                        } else {
                            ctx.response().end(result.cause().toString());
                            log.error("Error during query occured", result.cause());
                        }
                    });
        });
        httpServer.requestHandler(router);
        Future<HttpServer> httpServerDeployed = Future.future();
        httpServer.listen(API_SERVICE_VERTICLE_PORT, httpServerDeployed);
        httpServerDeployed.<Void>mapEmpty().setHandler(startFuture);
    }

}
