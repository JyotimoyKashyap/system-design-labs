package com.systemdesign.proxyserver.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProxyController {
    private final RestTemplate restTemplate;

    @Value("${api-server.url}")
    private String apiServerUrl;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleProxy(@NonNull HttpServletRequest request) throws URISyntaxException {
        // 1. PLACEHOLDER: Rate Limiting Check will go here later!
        // If limit exceeded -> return ResponseEntity.status(429).body("Too Many Requests");
        // 2. Extract the path the client is trying to reach (e.g., /api/v1/data)

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
