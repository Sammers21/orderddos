package com.orderddos.server;

import io.vertx.core.CompositeFuture;

import java.util.UUID;

public interface Undeployer {
    CompositeFuture undeployAttackWithUUID(UUID uuid);
}
