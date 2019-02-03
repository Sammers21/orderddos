package com.orderddos.server;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

public class DDoSApiService {

    private static Logger log = LoggerFactory.getLogger(DDoSApiService.class);

    public static String OTABEK_API_KEY = "1d0866bdd5ac3a73d043769679eb71e4b788ab28129dd4d22193bd05cd0ccc40";

    public static final String SETUP_ENV_SCRIPT;

    public static final Vertx VERTX;

    public static final String BASH_INTERPRETER_PATH = "/bin/bash";


    static {
        VERTX = Vertx.vertx();
        SETUP_ENV_SCRIPT = VERTX.fileSystem().readFileBlocking("env-dist-setup.yml").toString();
        System.out.println(SETUP_ENV_SCRIPT);
    }

    public static void main(String[] args) throws URISyntaxException {
        final URI uri = Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource("build_virus.sh" )
        ).toURI();
        System.out.println(uri);

        final ArrayList<String> processArgs = new ArrayList<>();
        processArgs.add(BASH_INTERPRETER_PATH);
        final ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
        processBuilder.directory(new File("/home/sammers/orderddos/api-server"));

        VERTX.deployVerticle(new ApiService(), event -> {
            if (event.succeeded()) {
                log.info("ApiService is on");
            }
        });
    }
}
