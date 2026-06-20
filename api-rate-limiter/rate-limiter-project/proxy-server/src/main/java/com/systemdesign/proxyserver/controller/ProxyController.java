package com.systemdesign.proxyserver.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.systemdesign.core.hashing.ConsistentHashingRing;
import com.systemdesign.core.ratelimit.RateLimiter;
import com.systemdesign.core.ratelimit.SlidingWindowRateLimiter;

import io.micrometer.common.lang.NonNull;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProxyController {
    private final RestTemplate restTemplate;

    @Value("${api-server.url}")
    private String apiServerUrl;

    @Value("${rate-limiter.max-requests}")
    private int maxRequests;
    @Value("${rate-limiter.window-size-seconds}")
    private int windowSizeSeconds;
    @Value("#{'${rate-limiter.redis-shards}'.split(',')}")
    private List<String> redisShards;

    private ConsistentHashingRing hashRing;
    private final Map<String, RateLimiter> limiters = new HashMap<>();

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        // 1. Initialize the Hash Ring with 100 virtual nodes per physical shard
        hashRing = new ConsistentHashingRing(100, redisShards);
        
        // 2. Create a connection pool for every Redis shard
        for (String shard : redisShards) {
            String[] parts = shard.trim().split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            limiters.put(shard.trim(), new SlidingWindowRateLimiter(host, port));
        }
    }


    @RequestMapping("/**")
    public ResponseEntity<String> handleProxy(@NonNull HttpServletRequest request) throws URISyntaxException {

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        String targetShard = hashRing.getNode(clientIp);
        RateLimiter limiter = limiters.get(targetShard);

        boolean isAllowed = limiter.isAllowed(clientIp, maxRequests, windowSizeSeconds);

        if (!isAllowed) {
            System.out.println("BLOCKED: " + clientIp + " (Exceeded " + maxRequests + " reqs/" + windowSizeSeconds + "s) ");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .body("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
        }

        String requestUri = request.getRequestURI();
        URI targetUri = new URI(apiServerUrl + requestUri);

        HttpHeaders httpHeaders = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> httpHeaders.add(headerName, request.getHeader(headerName)));

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        System.out.println("Forwarding request to : " + targetUri);
        ResponseEntity<String> response = restTemplate.exchange(
            targetUri,
            HttpMethod.valueOf(request.getMethod()),
            httpEntity,
            String.class
        );

        return response;
    }
}
