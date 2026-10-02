package com.jyotimoykashyap.policy;

import com.jyotimoykashyap.dto.RequestContext;

public interface PolicyResolver {
    Policy getPolicy(RequestContext requestContext);
}
