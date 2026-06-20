package com.systemdesign.core.ratelimit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SlidingWindowRateLimiter implements RateLimiter {

    private final JedisPool jedisPool;

    public SlidingWindowRateLimiter(String redisHost, int redisPort) {
        this.jedisPool = new JedisPool(redisHost, redisPort);
    }

    @Override
    public boolean isAllowed(String key, int maxRequests, int windowSizeSeconds) {
        long currentTimeMillis = System.currentTimeMillis();
        long windowStartMillis = currentTimeMillis - (windowSizeSeconds * 1000L);

        String member = currentTimeMillis + "-" + Math.random();

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.zremrangeByScore(key, 0, windowStartMillis);
            long requestCount = jedis.zcard(key);

            if (requestCount >= maxRequests) {
                return false;
            }

            jedis.zadd(key, currentTimeMillis, member);
            jedis.expire(key, windowSizeSeconds);
            return true;
        }
    }

}
