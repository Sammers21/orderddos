package com.orderddos.server;

import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Order {
    private final UUID uuid;
    private final OffsetDateTime t_submitted;
    private final String email;
    private final String target_url;
    private final JsonObject num_nodes_by_region;
    private final OffsetDateTime t_start;
    private final Interval duration;
    private final String status;

    public Order(UUID uuid, OffsetDateTime t_submitted, String email, String target_url, JsonObject num_nodes_by_region, OffsetDateTime t_start, Interval duration, String status) {
        this.uuid = uuid;
        this.t_submitted = t_submitted;
        this.email = email;
        this.target_url = target_url;
        this.num_nodes_by_region = num_nodes_by_region;
        this.t_start = t_start;
        this.duration = duration;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "uuid=" + uuid +
                ", t_submitted=" + t_submitted +
                ", email='" + email + '\'' +
                ", target_url='" + target_url + '\'' +
                ", num_nodes_by_region=" + num_nodes_by_region +
                ", t_start=" + t_start +
                ", duration=" + duration +
                ", status='" + status + '\'' +
                '}';
    }
}
