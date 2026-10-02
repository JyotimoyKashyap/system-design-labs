package com.jyotimoykashyap.ratelimiters;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.policy.Policy;

public class LeakyBucketRateLimiter implements ApiRateLimiter {

    @Override
    public boolean allowRequest(Policy policy, RequestContext requestContext) {
        return false;
    }
}
