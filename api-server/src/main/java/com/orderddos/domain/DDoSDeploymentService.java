package com.orderddos.domain;

import io.vertx.core.AbstractVerticle;

public class DDoSDeploymentService extends AbstractVerticle {

    public static final String DDOS_DEPLOYMENT_ADDRESS = "ddos.deployment";

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(DDOS_DEPLOYMENT_ADDRESS, deployRequest -> {
            DDoSRequest body = (DDoSRequest) deployRequest.body();

        });
    }
}
