package com.orderddos.domain;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.UUID;

import static com.orderddos.domain.DDoSDeploymentService.DDOS_DEPLOYMENT_ADDRESS;

public class ApiServiceVerticle extends AbstractVerticle {

    public static final Integer API_SERVICE_VERTICLE_PORT = 4000;

    public static final String START_DDOS = "start-ddos";
    public static final String URI = "uri";
    public static final String DURATION_SECONDS = "duration-seconds";
    public static final String SCALE = "scale";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        var httpServer = vertx.createHttpServer();
        var router = Router.router(vertx);

        router.post(START_DDOS).handler(ctx -> {
            ctx.request().bodyHandler(body -> {
                JsonObject jsonObject = body.toJsonObject();
                String uri = jsonObject.getString(URI);
                Integer durationSeconds = jsonObject.getInteger(DURATION_SECONDS);
                Integer scale = jsonObject.getInteger(SCALE);
                UUID uuid = UUID.randomUUID();
                vertx.eventBus().send(DDOS_DEPLOYMENT_ADDRESS, new DDoSRequest(uuid, scale, durationSeconds, uri));
                ctx.response().end(uuid.toString());
            });
        });

        httpServer.requestHandler(router);
        Future<HttpServer> httpServerDeployed = Future.future();
        httpServer.listen(API_SERVICE_VERTICLE_PORT, httpServerDeployed);
        httpServerDeployed.<Void>mapEmpty().setHandler(startFuture);
    }
}
