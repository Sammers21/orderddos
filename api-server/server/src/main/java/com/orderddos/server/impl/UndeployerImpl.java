package com.orderddos.server.impl;

import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Delete;
import com.orderddos.server.Undeployer;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class UndeployerImpl implements Undeployer {

    private static final Logger log = LoggerFactory.getLogger(UndeployerImpl.class);

    private final Vertx vertx;
    private final DigitalOceanClient digitalOceanClient;
    private PgPool pgPool;

    public UndeployerImpl(Vertx vertx, DigitalOceanClient digitalOceanClient, PgPool pgPool) {
        this.vertx = vertx;
        this.digitalOceanClient = digitalOceanClient;
        this.pgPool = pgPool;
    }

    @Override
    public CompositeFuture undeployAttackWithUUID(UUID uuid) {
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

        Future<PgRowSet> insertTStartFuture = Future.future();
        pgPool.preparedQuery("UPDATE Orders set status = 'DONE' where uuid = $1", Tuple.of(uuid), insertTStartFuture);
        List<Future> insertTStartFuture1 = List.of(insertTStartFuture, delete);
        return CompositeFuture.all(insertTStartFuture1);
    }
}
