package com.orderddos.server;

import com.myjeeva.digitalocean.pojo.Delete;
import io.vertx.core.Future;

import java.util.UUID;

public interface Undeployer {
    Future<Delete> undeployAttackWithUUID(UUID uuid);
}
