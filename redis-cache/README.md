# Redis Cache Lab

This project demonstrates how to use Redis as a caching layer in a Spring Boot application using the `@Cacheable` annotation. It utilizes the Cache-Aside pattern.

## Architecture
- **Backend**: Spring Boot 4 Application (`posts-service`)
- **Database**: H2 In-Memory Database (simulating a slow data source)
- **Cache**: Redis

## Caching Strategy
The service uses Spring's caching abstraction. When a request is made to fetch a post by its ID:
1. Spring checks Redis for the `posts::id` key.
2. **Cache Hit**: Data is returned instantly from Redis memory.
3. **Cache Miss**: Spring executes the service method, fetches data from H2 (simulating a 2-second delay), returns it, and automatically populates the Redis cache.

## Eviction Policy
Redis is configured with a `2mb` memory limit and the `allkeys-lru` (Least Recently Used) eviction policy. When the cache is full, the least recently requested posts are removed to make room for new ones.

## How to Run

1. Start the containers using Docker Compose:
   ```bash
   docker-compose up --build
   ```
2. Test the cache miss (takes ~2s):
   ```bash
   curl http://localhost:8080/api/posts/1
   ```
3. Test the cache hit (instant):
   ```bash
   curl http://localhost:8080/api/posts/1
   ```
