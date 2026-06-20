# Distributed API Rate Limiter

This project is a distributed rate limiter built with **Java Spring Boot**, **Redis**, and orchestrated entirely using **Docker Compose**. It handles rate limiting across multiple backend nodes by using a Consistent Hashing algorithm to route incoming IPs to specific Redis shards.

## 🏗 System Architecture

The architecture consists of 7 planned components, organized in a Gradle multi-module project:

1. **`proxy-server` (Gateway)**: A Spring Boot Edge server. It intercepts all incoming traffic, resolves the client's IP, and invokes the rate-limiting checks before forwarding traffic to the backend.
2. **`api-server` (Backend)**: A simple Spring Boot backend exposing the actual resources (e.g., `GET /api/v1/data`) that the user wants to consume.
3. **`rate-limiter-core` (Library)**: A pure Java library containing the mathematical **Consistent Hash Ring** and the **Sliding Window Rate Limiter** logic (powered by Jedis).
4. **Redis Shards**: 3 independent Redis instances acting as separate rate-limit counters to distribute the load across the cluster.
5. **Configuration System**: A single configuration defined via `application.yml` and Docker environment variables to dynamically assign the Redis shards.
6. **`admin-panel`**: (Pending) A lightweight Django dashboard to view real-time traffic and limit analytics.
7. **`web-visualizer`**: (Pending) A React/Vite Paper Brutalist visualizer.

## 🚀 How to Run

Because this is a fully distributed system, you do not need to boot the servers manually. Everything is containerized and orchestrated via Docker.

1. Navigate to the project root:
   ```bash
   cd rate-limiter-project
   ```
2. Build the JARs and boot the containers:
   ```bash
   docker-compose up -d --build
   ```

This spins up 5 containers on a private Docker network:
* `redis-shard-1`, `redis-shard-2`, `redis-shard-3`
* `api-server` (internal port 8081)
* `proxy-server` (exposed to host on port 8080)

## 🧪 How to Test & Spoof IPs

The proxy server extracts the user's IP address and uses it as the unique key in the Redis sliding window algorithm. 

To easily test rate limiting for multiple users without changing your physical machine's IP, the `proxy-server` respects the `X-Forwarded-For` HTTP header. 

You can use `curl` to send spoofed IP addresses and watch the system route traffic dynamically:

**Act as User A (IP: 192.168.1.50):**
```bash
curl -H "X-Forwarded-For: 192.168.1.50" http://localhost:8080/api/v1/data
```
*If you run this command 11 times within 60 seconds, User A will receive a `429 Too Many Requests` error.*

**Act as User B (IP: 10.0.0.99):**
```bash
curl -H "X-Forwarded-For: 10.0.0.99" http://localhost:8080/api/v1/data
```
*Even if User A is rate-limited, User B can still successfully fetch data because they have a completely separate token bucket!*

## 🛠 Tech Stack
- **Java 17** (Eclipse Temurin)
- **Spring Boot 3.4.2** (Web)
- **Gradle 9** (Multi-module build)
- **Redis 7** (Alpine)
- **Docker & Docker Compose**
