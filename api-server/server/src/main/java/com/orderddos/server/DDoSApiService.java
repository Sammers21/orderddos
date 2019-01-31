package com.orderddos.server;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDoSApiService {

    private static Logger log = LoggerFactory.getLogger(DDoSApiService.class);

    public static String OTABEK_API_KEY = "1d0866bdd5ac3a73d043769679eb71e4b788ab28129dd4d22193bd05cd0ccc40";

    public static final String SETUP_ENV_SCRIPT;

    public static final Vertx VERTX;

    static {
        VERTX = Vertx.vertx();
        SETUP_ENV_SCRIPT = VERTX.fileSystem().readFileBlocking("env-dist-setup.sh").toString();
        System.out.println(SETUP_ENV_SCRIPT);
    }

    public static void main(String[] args) {
        VERTX.deployVerticle(new ApiService(), event -> {
            if (event.succeeded()) {
                log.info("ApiService is on");
            }
        });
    }
}
