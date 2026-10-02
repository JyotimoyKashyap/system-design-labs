package com.jyotimoykashyap.ratelimiters;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.policy.Policy;

public interface ApiRateLimiter {
    boolean allowRequest(Policy policy, RequestContext requestContext);
}
