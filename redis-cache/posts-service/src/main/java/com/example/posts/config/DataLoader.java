package com.example.posts.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.posts.entity.Post;
import com.example.posts.repository.PostRepository;

@Configuration
public class DataLoader {
    
    @Bean
    CommandLineRunner initDatabase(PostRepository repository) {
        return args -> {
            repository.save(new Post("Alice", "My first post!", "https://example.com/alice.png"));
            repository.save(new Post("Bob", "Hello world from H2", "https://example.com/bob.png"));
            repository.save(new Post("Charlie", "Learning Redis Cache today", "https://example.com/charlie.png"));
            System.out.println("Mock posts loaded into H2 Database!");
        };
    }
}
