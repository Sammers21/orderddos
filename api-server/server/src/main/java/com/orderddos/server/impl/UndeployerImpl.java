package com.orderddos.server.impl;

import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Delete;
import com.orderddos.server.Undeployer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UndeployerImpl implements Undeployer {

    private static final Logger log = LoggerFactory.getLogger(UndeployerImpl.class);

    private final Vertx vertx;
    private final DigitalOceanClient digitalOceanClient;

    public UndeployerImpl(Vertx vertx, DigitalOceanClient digitalOceanClient) {
        this.vertx = vertx;
        this.digitalOceanClient = digitalOceanClient;
    }

    @Override
    public Future<Delete> undeployAttackWithUUID(UUID uuid) {
        io.vertx.core.Future<Delete> delete = io.vertx.core.Future.future();
        vertx.executeBlocking(event -> {
            try {
                final Delete removeDroplets = digitalOceanClient.deleteDropletByTagName(uuid.toString());
                log.info("Droplets  with tag '{}' has been removed", uuid.toString());
                event.complete(removeDroplets);
            } catch (Exception e) {
                log.error("Failed to delete order with uuid: {}", uuid, e);
                event.fail(e);
            }
        }, delete);
        return delete;
    }
}
