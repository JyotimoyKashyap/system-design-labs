package com.jyotimoykashyap.dto;

import java.util.Objects;

public record RequestContext(
        String clientId,
        Tier tier,
        String endpoint
) {
    public RequestContext {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be null or blank");
        }

        Objects.requireNonNull(tier, "tier cannot be null");

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint cannot be null or blank");
        }

        clientId = clientId.trim();
        endpoint = endpoint.trim();
    }
}
