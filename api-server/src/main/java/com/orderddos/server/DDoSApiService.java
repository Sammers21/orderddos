package com.orderddos.server;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDoSApiService {

    private static Logger log = LoggerFactory.getLogger(DDoSApiService.class);

    public static String OTABEK_API_KEY = "1d0866bdd5ac3a73d043769679eb71e4b788ab28129dd4d22193bd05cd0ccc40";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ApiService(), event -> {
            if (event.succeeded()) {
                log.info("ApiService is on ");
            }
        });
    }
}
