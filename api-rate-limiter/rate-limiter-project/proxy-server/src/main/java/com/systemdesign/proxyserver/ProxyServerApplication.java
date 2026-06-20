package com.systemdesign.proxyserver;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ProxyServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProxyServerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Disable the default behavior of throwing exceptions on 4xx and 5xx errors
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // Treat all HTTP responses as valid so we can forward them!
            }
        });
        
        return restTemplate;
    }
}
