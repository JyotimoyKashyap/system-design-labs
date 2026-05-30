package com.example.posts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.posts.entity.Post;
import com.example.posts.repository.PostRepository;

@Service
public class PostService {

    private static final Logger logger = 
    LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Cacheable(value = "posts", key = "#id")
    public Post getPostById(Long id) {

        logger.info("Cache miss! Fetching post with ID {} "
        + "directly from the H2 Database ... ", id);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return postRepository.findById(id)
                .orElseThrow(() -> 
                new RuntimeException("Post not found with id : " + id));
    }
}
