---
title: "API Rate Limiter Engine"
description: "Low-Level Design for a high-performance, thread-safe API rate limiter in Java supporting multiple algorithms."
publishedAt: "2026-07-20"
designPatterns: ["Strategy Pattern","Factory Pattern","Token Bucket","State Pattern"]
language: "Java / TypeScript"
githubRepo: "https://github.com/JyotimoyKashyap/system-design-labs/tree/main/api-rate-limiter"
difficulty: "Medium"
---

# API Rate Limiter — Low-Level Design

## 1. Problem Statement

Modern web applications, APIs, and microservice architectures are vulnerable to a wide variety of traffic anomalies:
1. **Denial-of-Service (DoS) & Brute Force Attacks:** Malicious actors bombarding login or checkout endpoints with automated scripts.
2. **Resource Starvation:** Aggressive web scrapers or poorly configured internal microservices monopolizing shared CPU, database connections, or downstream third-party quotas.
3. **Cascading Service Failures:** Uncontrolled traffic spikes causing downstream database connection pools to exhaust, triggering cascading outages across dependent services.

Building a production-grade **API Rate Limiter** requires solving four core low-level engineering challenges:
1. **Ultra-Low Latency Overhead (< 1ms):** Rate limiting sits directly on the critical request path. Every millisecond spent calculating limits directly adds latency to user requests.
2. **Deterministic & Fair Enforcement:** Rate limits must strictly adhere to configured quotas without dropping valid bursts or leaking extra quota across window boundaries.
3. **Thread Safety & High Concurrency:** Under tens of thousands of concurrent requests, counter updates must be atomic without causing global lock contention.
4. **Clean, Extensible Object-Oriented Architecture:** The engine must support multiple rate-limiting algorithms (Token Bucket, Leaky Bucket, Sliding Window Log) and allow interchangeable storage engines without rewriting business logic.

This document outlines the evolutionary engineering design—beginning with the **in-memory MVP foundation (Token Bucket)**, detailing its class relationships and mathematical subtleties, and establishing a roadmap for scaling into policy rules and distributed clusters.

---

## 2. Product Requirements

### 2.1 Core MVP Requirements

* **PR-1 (Request Interception & Decision):** When a request reaches the API gateway or controller, the rate limiter decides synchronously whether to allow the request through or reject it with an HTTP `429 Too Many Requests`.
* **PR-2 (Multi-Algorithm Support):** The design must support multiple rate-limiting strategies:
  * **Token Bucket:** Accommodates bursty traffic while enforcing an average sustained rate.
  * **Leaky Bucket:** Enforces smooth, constant-rate processing.
  * **Sliding Window Log:** Provides strictly accurate temporal limits without window-boundary anomalies.
* **PR-3 (Client Identification):** Requests are isolated by a unique `clientId` (such as an API Key, User ID, or IP address). A rate-limited client must never degrade or block traffic for other clients.

### 2.2 Extended Requirements (Future Scaling)

* **PR-4 (Tier-Based Rate Limiting):** The system should be able to enforce differentiated quotas based on user subscription tiers (e.g., Free vs. Pro vs. Enterprise tiers with distinct capacity and refill rates).
* **PR-5 (Endpoint-Specific Rate Limiting):** The rate limiter should be able to enforce custom rate limits per API endpoint or route (e.g., strict quotas for resource-heavy or sensitive paths like `POST /api/v1/auth/login`, contrasted with higher allowances for read-heavy routes like `GET /api/v1/products`).

---

## 3. Functional Requirements

### FR-1: Core Contract

```java
public interface ApiRateLimiter {
    boolean allowRequest(String clientId);
}
```

* **Contract Simplicity:** Returns a primitive `boolean` (`true` if allowed, `false` if rejected).
* **Zero Allocation on Hot Path:** Avoids wrapping results in wrapper objects or `Optional` to eliminate JVM garbage collection overhead on high-throughput paths.

---

### FR-2: Algorithm Parameters (Token Bucket)

* **Capacity ($C$):** The maximum number of tokens a bucket can hold at any instant. This defines the **maximum allowable burst**.
* **Refill Rate ($R$):** The number of tokens replenished per second. This defines the **sustained throughput limit**.

---

### FR-3: Scope Boundaries (MVP)

* **In-Memory Operation:** The initial MVP operates in-memory within a single JVM process.
* **Cold Start Guarantee:** A newly observed client starts with a full bucket (`tokens = capacity`), allowing legitimate initial bursts without false rejections.
* **Client Isolation:** Each client possesses their own isolated bucket state.

---

## 4. Step 1: The Baseline Foundation (MVP Design)

The MVP focuses on an elegant, extensible object-oriented structure implementing the **Token Bucket** algorithm, while preparing extension points for Leaky Bucket and Sliding Window Log.

### 4.1 The Core MVP Entities

```
[ Client / Application ]
           │
           ▼
[ ApiRateLimiterService ] ──(has-a)──► [ ApiRateLimiter ]
                                              ▲
                                              │ (creates)
                                  [ ApiRateLimiterFactory ]
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │ (implements)            │ (implements)            │ (implements)
                    ▼                         ▼                         ▼
      [ TokenBucketRateLimiter ]   [ LeakyBucketRateLimiter ]  [ SlidingWindowLogRateLimiter ]
                    │
                    ▼ (composition)
              [ TokenBucket ]
```

1. **`ApiRateLimiterService` (Gateway Facade):** The single entry point for applications. Holds a reference to an `ApiRateLimiter` and delegates request admission checks.
2. **`ApiRateLimiter` (Strategy Interface):** The core contract defining `allowRequest(String clientId)`. Adheres to the **Open/Closed Principle**, enabling transparent strategy switching.
3. **`ApiRateLimiterFactory` (Creational Factory):** Encapsulates the instantiation of concrete rate limiters (`createTokenBucketLimiter()`, `createSlidingWindowLogLimiter()`, `createLeakyBucketLimiter()`), shielding callers from constructor complexity.
4. **`TokenBucketRateLimiter` (Algorithm Coordinator):** Manages a thread-safe registry of client buckets (`Map<String, TokenBucket>`) along with default capacity and refill settings.
5. **`TokenBucket` (Client State Engine):** Represents an individual client's bucket. Holds current tokens and timestamps, and evaluates lazy replenishment math atomically upon each request.
6. **`LeakyBucketRateLimiter` & `SlidingWindowLogRateLimiter`:** Concrete implementation placeholders ready for upcoming milestones.

---

### 4.2 Class Diagram

```mermaid
classDiagram
    direction TB

    class ApiRateLimiterService {
        - apiRateLimiter: ApiRateLimiter
        + ApiRateLimiterService(apiRateLimiter: ApiRateLimiter)
        + allowRequest(clientId: String): Boolean
    }

    class ApiRateLimiter {
        <<interface>>
        + allowRequest(clientId: String): Boolean
    }

    class ApiRateLimiterFactory {
        + createTokenBucketLimiter(): ApiRateLimiter
        + createSlidingWindowLogLimiter(): ApiRateLimiter
        + createLeakyBucketLimiter(): ApiRateLimiter
    }

    class TokenBucketRateLimiter {
        - map: Map~String, TokenBucket~
        - capacity: long
        - refillTokensPerSecond: long
        + allowRequest(clientId: String): Boolean
    }

    class LeakyBucketRateLimiter {
        + allowRequest(clientId: String): Boolean
    }

    class SlidingWindowLogRateLimiter {
        + allowRequest(clientId: String): Boolean
    }

    class TokenBucket {
        - capacity: long
        - refillTokensPerSecond: long
        - tokens: long
        - lastRefillTimestamp: long
        + tryConsume(tokensRequested: int): boolean
        - refill(): void
    }

    ApiRateLimiterService --> ApiRateLimiter : has-a
    ApiRateLimiterFactory ..> ApiRateLimiter : «creates»
    TokenBucketRateLimiter ..|> ApiRateLimiter : implements
    LeakyBucketRateLimiter ..|> ApiRateLimiter : implements
    SlidingWindowLogRateLimiter ..|> ApiRateLimiter : implements
    TokenBucketRateLimiter *-- TokenBucket : composition
```

> **Design Decision: Why skip `AbstractApiRateLimiter` for the MVP?**  
> While an abstract class could theoretically sit between the interface and implementations, different rate-limiting algorithms have vastly distinct internal states (Token Bucket uses counters and timestamps, Leaky Bucket uses queues or leak intervals, Sliding Window Log uses sorted timestamp collections). In accordance with **YAGNI (You Aren't Gonna Need It)**, we avoid premature inheritance until clear common logic emerges across multiple algorithms.

---

### 4.3 The Core Algorithm: "Lazy Refill" Token Bucket

A common anti-pattern in naive rate limiters is running a background daemon timer (e.g. `ScheduledExecutorService`) that ticks every second to add tokens to all client buckets:
* **The Background Timer Flaw:** If a service has 500,000 unique client buckets in memory, the background worker must iterate through 500,000 objects every second—burning CPU cycles and causing lock contention, even for clients that have been idle for days.

#### The Lazy Refill Solution
Instead of proactively pushing tokens, we calculate replenished tokens **on-demand** when a client request arrives:

$$\Delta t = t_{\text{now}} - t_{\text{lastRefill}}$$
$$\text{newTokens} = \frac{\Delta t \times \text{refillTokensPerSecond}}{1000}$$
$$\text{tokens} = \min(\text{capacity}, \text{tokens} + \text{newTokens})$$

---

### 4.4 The Integer Remainder Retention Trick 🧮

When calculating token replenishment using integer math, standard division drops remainders:
* Suppose `refillTokensPerSecond = 5` ($1\text{ token every } 200\text{ ms}$).
* If requests arrive every $100\text{ ms}$:
  * $\text{tokensToAdd} = (100 \times 5) / 1000 = 0$.
  * If `lastRefillTimestamp` is unconditionally updated to $t_{\text{now}}$, those $100\text{ ms}$ are **discarded forever**. The client would never receive tokens despite waiting patiently!

#### Preserving Sub-Token Time
To achieve 100% precision using pure `long` integers without floating-point drift, we only advance `lastRefillTimestamp` by the time **actually converted into whole tokens**:

```java
private void refill() {
    long now = System.currentTimeMillis();
    long elapsedMs = now - lastRefillTimestamp;

    long tokensToAdd = (elapsedMs * refillTokensPerSecond) / 1000;

    if (tokensToAdd > 0) {
        long newTokens = tokens + tokensToAdd;
        if (newTokens >= capacity) {
            tokens = capacity;
            lastRefillTimestamp = now; // Bucket full; discard overflow time
        } else {
            tokens = newTokens;
            long timeUsedMs = (tokensToAdd * 1000) / refillTokensPerSecond;
            lastRefillTimestamp += timeUsedMs; // Preserve unused fractional time
        }
    }
}
```

#### Numerical Trace Example
* **Configuration:** `capacity = 10`, `refillRate = 5 tokens/sec` (1 token per $200\text{ ms}$).

| Time ($T$) | Event | Elapsed ($\Delta t$) | `tokensToAdd` | Action | Resulting `tokens` | `lastRefillTimestamp` |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **$0\text{ ms}$** | Bucket created & Req #1 arrives | $0\text{ ms}$ | $0$ | Consume 1 token | $9$ | $0\text{ ms}$ |
| **$100\text{ ms}$** | Req #2 arrives | $100\text{ ms}$ | $(100 \times 5)/1000 = 0$ | No tokens added; timestamp **not moved** | $8$ (consumed) | $0\text{ ms}$ (preserved!) |
| **$200\text{ ms}$** | Req #3 arrives | $200 - 0 = 200\text{ ms}$ | $(200 \times 5)/1000 = 1$ | 1 token added; timestamp moves by $200\text{ ms}$ | $8$ ($8 + 1 - 1$) | $200\text{ ms}$ |
| **$100,000\text{ ms}$** | User returns after long idle | $99,800\text{ ms}$ | $(99,800 \times 5)/1000 = 499$ | Tokens cap at 10; timestamp resets to $now$ | $9$ (consumed) | $100,000\text{ ms}$ |

---

### 4.5 Concurrency & Thread-Safety Model

High-throughput systems require strict concurrency guarantees without coarse-grained bottlenecks:

1. **Bucket Registry (`ConcurrentHashMap`):**
   * Buckets are indexed by `clientId`.
   * Newly seen clients instantiate their bucket via `computeIfAbsent()`:
     ```java
     TokenBucket bucket = map.computeIfAbsent(
         clientId, 
         k -> new TokenBucket(capacity, refillTokensPerSecond)
     );
     ```
2. **Fine-Grained Bucket Locking (`synchronized` on Bucket):**
   * Thread synchronization is isolated strictly to the individual `TokenBucket` instance:
     ```java
     public synchronized boolean tryConsume(int tokensRequested) { ... }
     ```
   * **Why this scales:** 10,000 concurrent threads making requests for different clients execute in parallel without waiting on each other. Only concurrent requests for the **same client** serialize momentarily for a few CPU instructions.

---

---

## 5. Step 2: Policy & Request Context Layer (Tiers & Endpoint Routing)

To support **PR-4 (Tier-Based Limits)** and **PR-5 (Endpoint-Specific Limits)**, we decouple **policy resolution** (business rules) from **rate enforcement** (algorithm execution).

### 5.1 Architecture & Class Connections

```mermaid
classDiagram
    direction TB

    class RequestContext {
        + clientId: String
        + tier: Tier
        + endpoint: String
    }

    class Tier {
        <<enum>>
        FREE
        PREMIUM
        ENTERPRISE
    }

    class Policy {
        <<record>>
        + limit: long
        + timeWindow: long
        + burstCapacity: long
    }

    class PolicyResolver {
        <<interface>>
        + getPolicy(request: RequestContext): Policy
    }

    class ApiRateLimiterService {
        - apiRateLimiter: ApiRateLimiter
        - policyResolver: PolicyResolver
        + allowRequest(request: RequestContext): Boolean
    }

    class ApiRateLimiter {
        <<interface>>
        + allowRequest(key: String, policy: Policy): Boolean
    }

    class ApiRateLimiterFactory {
        + createTokenBucketLimiter(): ApiRateLimiter
        + createSlidingWindowLogLimiter(): ApiRateLimiter
        + createLeakyBucketLimiter(): ApiRateLimiter
    }

    RequestContext --> Tier : has-a
    PolicyResolver ..> RequestContext : inspects
    PolicyResolver ..> Policy : resolves & returns
    ApiRateLimiterService --> PolicyResolver : has-a (resolves rule)
    ApiRateLimiterService --> ApiRateLimiter : has-a (enforces rule)
    ApiRateLimiterFactory ..> ApiRateLimiter : «creates»
    ApiRateLimiter ..> Policy : evaluates with
```

### 5.2 How the Components Connect

1. **`RequestContext` $\longrightarrow$ `Tier` (Association):**
   * Encapsulates caller identity (`clientId`), subscription plan (`tier`), and target path (`endpoint`).
   * Created at the gateway/filter level before rate limiting is evaluated.

2. **`ApiRateLimiterService` $\longrightarrow$ `PolicyResolver` (Association):**
   * The service holds a reference to a `PolicyResolver`.
   * When `allowRequest(requestContext)` is invoked, the service asks the resolver: *"What is the quota for this user tier and endpoint?"*

3. **`PolicyResolver` $-\;-\;\longrightarrow$ `Policy` (Dependency):**
   * Inspects `RequestContext` and returns an immutable `Policy` containing `(limit, timeWindow, burstCapacity)`.
   * Encapsulates precedence rules (e.g., endpoint-specific rules taking priority over global tier rules).

4. **`ApiRateLimiterService` $\longrightarrow$ `ApiRateLimiter` (Association):**
   * The service holds the algorithm engine (injected at startup).
   * It builds a composite rate-limit key (e.g. `clientId + ":" + endpoint`) and delegates enforcement:
     ```java
     Policy policy = policyResolver.getPolicy(requestContext);
     String key = requestContext.clientId() + ":" + requestContext.endpoint();
     return apiRateLimiter.allowRequest(key, policy);
     ```

5. **`ApiRateLimiterFactory` $-\;-\;\longrightarrow$ `ApiRateLimiter` (Creational Dependency):**
   * Encapsulates algorithm instantiation (`createTokenBucketLimiter()`), shielding `ApiRateLimiterService` from concrete constructor details.

---

### 5.3 Request Execution Flow

```mermaid
sequenceDiagram
    autonumber
    actor Client as API Gateway / Controller
    participant Service as ApiRateLimiterService
    participant Resolver as PolicyResolver
    participant Limiter as ApiRateLimiter (e.g., TokenBucket)

    Client->>Service: allowRequest(RequestContext)
    Service->>Resolver: getPolicy(RequestContext)
    Resolver-->>Service: return Policy(limit, timeWindow, burstCapacity)
    Note over Service: Construct key = clientId + ":" + endpoint
    Service->>Limiter: allowRequest(key, Policy)
    Limiter-->>Service: return true (Allowed) or false (429)
    Service-->>Client: return boolean decision
```

---

## 6. Evolutionary Roadmap (Scaling Beyond)

The following milestones outline the evolutionary path from the single-process engine to an enterprise-grade distributed system:

```
Step 1: MVP (Token Bucket + In-Memory Map)
    │
    ▼
Step 2: Policy & Request Context Layer (User Tiers & Endpoint Routing)
    │
    ▼
Step 3: Alternative Algorithm Engines (Leaky Bucket, Sliding Window Log, Sliding Window Counter)
    │
    ▼
Step 4: Memory Hygiene & Cleanup (Eviction of Stale/Idle Buckets via TTL & Weak References)
    │
    ▼
Step 5: Distributed Multi-Node Enforcement (Redis Cluster, Consistent Hash Ring, Atomic Lua Scripts)
```

1. **Milestone 3 (Alternative Algorithms):** Implement `LeakyBucketRateLimiter` (FIFO queue for smooth egress) and `SlidingWindowLogRateLimiter` (rolling timestamp log).
2. **Milestone 4 (Memory Hygiene):** Add automated eviction for idle client buckets to prevent heap exhaustion over months of continuous uptime.
3. **Milestone 5 (Distributed Architecture):** Migrate bucket counters to a distributed cache (Redis) using atomic Lua scripts or consistent hashing across multi-node API gateways.
