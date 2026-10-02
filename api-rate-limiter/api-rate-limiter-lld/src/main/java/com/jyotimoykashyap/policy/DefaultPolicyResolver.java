package com.jyotimoykashyap.policy;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.dto.Tier;

import java.util.Map;

public class DefaultPolicyResolver implements PolicyResolver {

    private final Map<Tier, Policy> tierPolicies = Map.of(
            Tier.FREE, new Policy(10, 60, 10),
            Tier.PREMIUM, new Policy(100, 60, 50),
            Tier.ENTERPRISE, new Policy(1000, 60, 500)
    );

    @Override
    public Policy getPolicy(RequestContext requestContext) {
        if (requestContext == null || requestContext.tier() == null) {
            return tierPolicies.get(Tier.FREE);
        }
        return tierPolicies.getOrDefault(requestContext.tier(), tierPolicies.get(Tier.FREE));
    }
}
