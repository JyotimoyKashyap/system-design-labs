package com.jyotimoykashyap;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.policy.Policy;
import com.jyotimoykashyap.policy.PolicyResolver;
import com.jyotimoykashyap.ratelimiters.ApiRateLimiter;

import java.util.Objects;

public class ApiRateLimiterService {

    private final ApiRateLimiter apiRateLimiter;
    private final PolicyResolver policyResolver;

    public ApiRateLimiterService(ApiRateLimiter apiRateLimiter, PolicyResolver policyResolver) {
        this.apiRateLimiter = Objects.requireNonNull(apiRateLimiter, "apiRateLimiter cannot be null");
        this.policyResolver = Objects.requireNonNull(policyResolver, "policyResolver cannot be null");
    }

    public boolean allowRequest(RequestContext requestContext) {
        Policy policy = policyResolver.getPolicy(requestContext);
        return apiRateLimiter.allowRequest(policy, requestContext);
    }
}
