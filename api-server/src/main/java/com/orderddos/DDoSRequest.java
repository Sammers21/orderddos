package com.orderddos;

import java.util.UUID;

public class DDoSRequest {
    UUID ddosId;
    Integer scale;
    Integer durationSeconds;
    String uri;

    public DDoSRequest(UUID ddosId, Integer scale, Integer durationSeconds, String uri) {
        this.ddosId = ddosId;
        this.scale = scale;
        this.durationSeconds = durationSeconds;
        this.uri = uri;
    }

    public UUID getDdosId() {
        return ddosId;
    }

    public Integer getScale() {
        return scale;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public String getUri() {
        return uri;
    }
}
