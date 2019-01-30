package com.orderddos.server;

import io.vertx.core.Vertx;

public class DdosApiService {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ApiService());
    }
}
